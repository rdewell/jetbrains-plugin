package io.stacklane.jetbrains.client;

import io.stacklane.jetbrains.output.BuildOutputConsole;
import io.stacklane.jetbrains.output.BuildOutputEntry;
import io.stacklane.jetbrains.output.BuildOutputEntryType;
import mjson.Json;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Predicate;

/**
 *
 */
public class UploadClient {
    /**
     * As long as this JVM / plugin is open, you'll have a single session ID.
     *
     * Not a big deal for this to change over time, though it main purpose is to isolate anonymous project IDs.
     *
     * Of course this *assumes* the plugin classes are not somehow loaded/unloaded frequently
     * within a single running app.
     */
    private final static String MACHINE_SESSION_ANON_ID = UUID.randomUUID().toString();

    private final Path source;
    private final String auth;
    private final String projectId;

    private static final HttpClient CLIENT = HttpClients.custom().disableRedirectHandling().build();

    public static UploadClient anon(Path projectPath){
        return new UploadClient(Optional.empty(), projectPath);
    }

    /**
     * @param machineId -- Adds more qualification to the {@param source}
     * @param source
     */
    private UploadClient(Optional<String> machineId, Path source){
        this.source = source;
        this.auth = "Bearer anonymous";

        if (machineId.isPresent()){
            this.projectId = encodeProjectId(machineId.get() + source.toString());
        } else {
            this.projectId = encodeProjectId(MACHINE_SESSION_ANON_ID + source.toString());
        }
    }

    private static String encodeProjectId(String base) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            return Base64.encodeBase64URLSafeString(hash);
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private URI getEndpoint(){
        return URI.create(UploadClientSettings.ENDPOINT + "/" + projectId);
    }

    /**
     * @param console
     * @return The {@link URI} to open in a browser.
     * @throws Exception
     */
    public URI build(BuildOutputConsole console, Optional<Json> buildProps) throws Exception{
        console.entry(BuildOutputEntry.info("Starting build process"));

        final HttpPost post = new HttpPost();

        post.setHeader("Authorization", auth);
        post.setHeader("Accept", "application/json"); // Use JSON build output option
        post.setURI(getEndpoint());

        if (buildProps.isPresent()){
            post.setHeader("Content-Type", "application/json; charset=UTF-8");
            post.setEntity(new StringEntity(buildProps.get().toString(), StandardCharsets.UTF_8));
        }

        try {
            HttpResponse response = CLIENT.execute(post);

            if (response.getStatusLine().getStatusCode() == 404){
                /**
                 * 404 indicates that entire project should be re-PUT,
                 * and then operation retried.
                 */
                try {
                    putAll(console);
                } catch (Throwable t){
                    console.error("Error uploading source code", t);
                    throw new Exception();
                }

                response = CLIENT.execute(post);
            }

            if (response.getStatusLine().getStatusCode() != 200){
                console.error("API returned " + response.getStatusLine().getStatusCode());
                throw new UploadStatusException(response);
            }

            try (final InputStream is = response.getEntity().getContent()){
                final BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String currentLine = null;
                URI success = null;
                BuildOutputEntry entry = null;

                while ((currentLine = in.readLine()) != null) {
                    entry = BuildOutputEntry.parse(currentLine);
                    console.entry(entry);

                    if (entry.getType() == BuildOutputEntryType.RESULT) {
                        hitURL(entry.getResult());
                        success = entry.getResult();
                    }
                }

                if (success == null){
                    // Only emit this if last entry was not an ERROR:
                    console.error("Build failed");
                    throw new Exception();
                }

                return success;
            }
        } catch (IOException io){
            console.error("Build API error", io);
            throw io;
        } finally {
            post.releaseConnection();
        }
    }

    private static void hitURL(URI url){
        /**
         * This step ensures that test domain is loaded and ready geographically close to user.
         * But it could actually end up hitting a different server.
         *
         * TODO but better would be to also scrape the CSS/JS/IMAGES and load as well, so they
         *  are at a geographically close CDN.
         *  or use a library that is aware of these when loading the page.
         */
        final HttpGet get = new HttpGet(url);

        try {
            CLIENT.execute(get);
        } catch (Throwable ignoreOptionalOperation){

        } finally{
            get.releaseConnection();
        }
    }

    public Json getManifest() throws IOException, UploadStatusException {
        final HttpGet get = new HttpGet();

        get.setHeader("Authorization", auth);
        get.setHeader("Accept", "application/json");
        get.setURI(getEndpoint());

        try {

            final ByteArrayOutputStream bao = new ByteArrayOutputStream();

            final HttpResponse response = CLIENT.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                response.getEntity().writeTo(bao);

                final String s = bao.toString(StandardCharsets.UTF_8.name());

                return Json.read(s);
            } else {
                throw new UploadStatusException(response);
            }

        } finally {

            try {
                get.releaseConnection();
            } catch (Throwable ignore){};

        }
    }

    /**
     * This will fallback to {@link #putAll}} if the project doesn't exist.
     */
    public void putIncrementally(BuildOutputConsole console) throws IOException, UploadStatusException {
        Json manifest = null;

        console.info("Checking for files to sync");

        try {
            manifest = getManifest();
        } catch (UploadStatusException u){
            if (u.getStatus() == 404){
                putAll(console); // Fallback scenario, required if project does not exist
                return;
            } else {
                throw u;
            }
        }

        // We have a manifest, now create a delete/update plan.

        final Set<String> retainFromManifest = new HashSet<>();
        final Set<String> updateFromManifest = new HashSet<>();

        final List<Json> manifestFiles = manifest.at("files").asJsonList();

        for (final Json file : manifestFiles){
            final String manifestPath = file.at("path").asString();

            final String relative = manifestPath.substring(1);

            final File f = new File(source.toFile(), relative);

            if (f.exists()){
                retainFromManifest.add(manifestPath);

                FileInputStream fis = new FileInputStream(f);

                byte[] md5 = org.apache.commons.codec.digest.DigestUtils.md5(fis);

                final String base64 = Base64.encodeBase64String(md5);

                if (!file.at("md5").asString().equals(base64)){
                    updateFromManifest.add(manifestPath);
                }

                fis.close();
            }
        }

        if (retainFromManifest.size() < manifestFiles.size()){
            final long toPrune = (manifestFiles.size() - retainFromManifest.size());

            // There were some missing, which implies there are some to delete.
            console.info("Pruning " + toPrune + " file" + ((toPrune > 1) ? "s" : ""));

            final HttpPatch del = new HttpPatch();

            del.setHeader("Authorization", auth);
            del.setHeader("Content-Type", "application/json");
            del.setHeader("Accept", "application/json");
            del.setURI(getEndpoint());

            Json obj = Json.object();
            Json arr = Json.array();

            for (final String r : retainFromManifest){
                arr.add(r);
            }

            obj.set("retain", arr);

            del.setEntity(new StringEntity(obj.toString(), StandardCharsets.UTF_8));

            final HttpResponse response = CLIENT.execute(del);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new UploadStatusException(response);
            }
        }

        final Predicate<Path> shouldUpdate = path -> {
            final String projectAbs = "/" + source.relativize(path).toString();

            /**
             * MD5 changed, see above
             */
            if (updateFromManifest.contains(projectAbs)) return true;

            /**
             * The file exists in both locations, but MD5 did not change, so no update, see above.
             */
            if (retainFromManifest.contains(projectAbs)) return false;

            /**
             * This is a new file that was not in the manifest.
             */
            return true;
        };

        final long toUpdate = UploadUtil.getZippableFileTotal(source, shouldUpdate);

        if (toUpdate == 0) {
            console.info("Sync complete");
            return;
        }

        console.info("Syncing " + toUpdate + " file" + ((toUpdate > 1) ? "s" : "") );

        final HttpPatch patch = new HttpPatch();

        patch.setHeader("Authorization", auth);
        patch.setHeader("Content-Type", "application/zip");
        patch.setHeader("Accept", "application/json");
        patch.setURI(getEndpoint());

        uploadFiles(patch, shouldUpdate, console);
    }

    public void putAll(BuildOutputConsole console) throws IOException, UploadStatusException {
        final HttpPut put = new HttpPut();

        put.setHeader("Authorization", auth);
        put.setHeader("Content-Type", "application/zip");
        put.setHeader("Accept", "application/json");
        put.setURI(getEndpoint());

        final Predicate<Path> all = path -> true;

        final long toUpdate = UploadUtil.getZippableFileTotal(source, all);
        console.info("Syncing all " + toUpdate + " file" + ((toUpdate > 1) ? "s" : "")  );

        uploadFiles(put, all, console);
    }

    private void uploadFiles(HttpEntityEnclosingRequestBase put, Predicate<Path> filePaths, BuildOutputConsole console) throws IOException, UploadStatusException {
        Path zipped = null;

        try {

            zipped = UploadUtil.zip(source, filePaths);

            InputStreamEntity isEntity = new InputStreamEntity(new FileInputStream(zipped.toFile()));

            put.setEntity(isEntity);

            final HttpResponse response = CLIENT.execute(put);

            if (response.getStatusLine().getStatusCode() == 200){

                /*
                final ByteArrayOutputStream bao = new ByteArrayOutputStream();
                response.getEntity().writeTo(bao);
                final String s = bao.toString("UTF-8");
                return Json.read(s);*/
                return;

            } else {

                throw new UploadStatusException(response);

            }

        } finally {

            try {
                zipped.toFile().delete();
                //console.info(zipped.toString());
            } catch (Throwable ignore){}

            try {
                put.releaseConnection();
            } catch (Throwable ignore){};

        }
    }
}
