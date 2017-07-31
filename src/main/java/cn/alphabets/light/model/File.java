package cn.alphabets.light.model;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.datarider.Rider;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
import static io.vertx.core.http.HttpHeaders.*;

/**
 * File
 * Created by lilin on 2016/11/12.
 */
public class File {
    private static final Logger logger = LoggerFactory.getLogger(File.class);

    /**
     * add one or more files
     *
     * @param handler context
     * @return meta
     * @throws BadRequestException add error
     */
    public Plural<ModFile> add(Context handler) throws BadRequestException {

        List<RequestFile> files = handler.params.getFiles();
        if (files == null) {
            throw new BadRequestException("No file content found.");
        }

        List<ModFile> result = new ArrayList<>();
        files.forEach(file -> {

            // RDB 的文件支持
            if (Environment.instance().isRDB()) {
                result.add(addToMySQL(handler, file));
                return;
            }

            GridFSFile gfsFile = new Controller(handler).writeFileToGrid(file);

            // set file information
            ModFile info = new ModFile();
            info.setName(gfsFile.getFilename());
            info.setLength(gfsFile.getLength());
            info.setContentType(gfsFile.getMetadata().getString("contentType"));
            info.setFileId(gfsFile.getObjectId());

            // copy option information
            info.setKind(handler.params.getData().getString("kind"));
            info.setDescription(handler.params.getData().getString("description"));
            info.setType(handler.params.getData().getString("type"));
            info.setPath(handler.params.getData().getString("path"));

            handler.params.data(info);
            Singular<ModFile> saved = Rider.add(handler, ModFile.class);

            // delete tmp file after upload to grid fs
            if (!file.isKeep()) {
                boolean state = new java.io.File(file.getFilePath()).delete();
                if (!state) {
                    logger.warn("Failed to delete temporary files");
                }
            }

            logger.debug("file upload done : " + saved.item.toDocument().toJson());
            result.add(saved.item);
        });

        return new Plural<>((long) result.size(), result);
    }

    ModFile addToMySQL(Context handler, RequestFile file) {

        Params params = handler.params;
        cn.alphabets.light.db.mysql.Controller ctrl = new cn.alphabets.light.db.mysql.Controller(handler, params);

        ModFile info = ctrl.writeFile(file).item;
        Document source = handler.params.getData();

        // set file information
        info.setName(file.getFileName());
        info.setContentType(file.getContentType());

        // set option information
        info.setKind(source.getString("kind"));
        info.setDescription(source.getString("description"));
        info.setType(source.getString("type"));
        info.setPath(source.getString("path"));

        // update meta info
        Rider.update(handler, ModFile.class, new Params().data(info).id(info.get_id()));
        return info;
    }

    /**
     * Physically delete the file, and also delete the GridFS file
     *
     * @param handler context
     * @return removed db record count
     * @throws LightException remove error
     */
    public long remove(Context handler) throws LightException {

        // step 1. get meta
        Singular<ModFile> info = Rider.get(handler, ModFile.class);
        if (info.item == null) {
            throw new BadRequestException("File not exist.");
        }
        logger.warn(info.item.toDocument().toString());

        // step 2. delete file from grid fs
        try {
            new Controller(handler, new Params().id(info.item.getFileId())).deleteFromGrid();
        } catch (Exception e) {
            logger.error("File content delete failed : ", e);
            throw new BadRequestException("File content delete failed.");
        }

        // step 3. delete file info
        return Rider.remove(handler, ModFile.class);
    }

    /**
     * Picture download
     *
     * @param handler context
     * @throws LightException file not found
     */
    public void image(Context handler) throws LightException {
        this.download(handler);
    }

    /**
     * Save file to specific path.
     * <p>
     * The path size is given by{@link ModFile#getPath()}
     *
     * @param handler context
     * @param file    ModFile instance
     */
    public void saveFile(Context handler, ModFile file) {

        if (Environment.instance().isRDB()) {
            saveFileFromMySQL(handler, file);
            return;
        }

        Controller ctrl = new Controller(handler, new Params().id(file.getFileId()));

        try {
            FileOutputStream outputStream = new FileOutputStream(file.getPath());
            ctrl.readStreamFromGrid(outputStream);
            outputStream.close();
        } catch (IOException e) {
            logger.error("error read file : ", e);
            throw new RuntimeException("File not exist.");
        }
    }

    void saveFileFromMySQL(Context handler, ModFile file) {

        Params params = handler.params;
        cn.alphabets.light.db.mysql.Controller ctrl = new cn.alphabets.light.db.mysql.Controller(handler, params);
        ByteArrayOutputStream stream = ctrl.readFile();

        try {
            stream.writeTo(new FileOutputStream(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException("File not exist.");
        }
    }

    public void sendFile(Context handler, ModFile file) {

        // Range: bytes=0-801 offset:0 length:801
        String range = handler.req().getHeader("Range");

        Controller ctrl = new Controller(handler, new Params().id(file.getFileId()));

        if (StringUtils.isEmpty(range)) {
            // get entire file content
            ByteArrayOutputStream content = ctrl.readStreamFromGrid();

            handler.res().putHeader(ACCEPT_RANGES, "bytes")
                    .putHeader(CONTENT_TYPE, file.getContentType())
                    .putHeader(CONTENT_LENGTH, String.valueOf(file.getLength()))
                    .putHeader(CACHE_CONTROL, "public, max-age=34560000")
                    .putHeader(LAST_MODIFIED, Helper.toUTCString(file.getUpdateAt()))
                    .write(Buffer.buffer(content.toByteArray()))
                    .end();
            return;
        }

        Pattern patternRange = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

        // end byte is length - 1
        long end = file.getLength() - 1;

        long offset = -1;

        Matcher m = patternRange.matcher(range);
        try {
            if (m.matches()) {

                String part = m.group(1);
                // offset cannot be empty
                offset = Long.parseLong(part);
                // offset must fall inside the limits of the file
                if (offset < 0 || offset >= file.getLength()) {
                    throw new IndexOutOfBoundsException();
                }
                // length can be empty
                part = m.group(2);
                if (part != null && part.length() > 0) {
                    // ranges are inclusive
                    end = Long.parseLong(part);
                    // offset must fall inside the limits of the file
                    if (end < offset || end >= file.getLength()) {
                        throw new IndexOutOfBoundsException();
                    }
                }

            } else {
                throw new IndexOutOfBoundsException();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            handler.res().putHeader("Content-Range", "bytes */" + file.getLength())
                    .setStatusCode(REQUESTED_RANGE_NOT_SATISFIABLE.code())
                    .end(REQUESTED_RANGE_NOT_SATISFIABLE.reasonPhrase());

        }

        offset = Math.max(offset, 0);

        handler.res().putHeader(ACCEPT_RANGES, "bytes")
                .putHeader(CONTENT_TYPE, file.getContentType())
                .putHeader(CONTENT_LENGTH, Long.toString(end + 1 - offset))
                .putHeader(CACHE_CONTROL, "public, max-age=34560000")
                .putHeader(LAST_MODIFIED, Helper.toUTCString(file.getUpdateAt()));

        if (handler.req().method() == HttpMethod.HEAD) {
            handler.res().end();
            return;
        }

        ByteArrayOutputStream content = ctrl.readStreamFromGrid(offset, end - offset + 1);

        handler.res().putHeader(CONTENT_RANGE, "bytes " + offset + "-" + end + "/" + file.getLength())
                .setStatusCode(PARTIAL_CONTENT.code())
                .write(Buffer.buffer(content.toByteArray()))
                .end();

    }

    public void download(Context handler) throws LightException {

        try {
            Singular<ModFile> file = Rider.get(handler, ModFile.class);
            sendFile(handler, file.item);
        } catch (Exception e) {
            logger.error("error read file : ", e);
            throw new BadRequestException("File not exist.");
        }
    }

    // TODO: update
    // TODO: zip
    // TODO: PDF
}
