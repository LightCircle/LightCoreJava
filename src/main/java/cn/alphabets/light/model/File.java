package cn.alphabets.light.model;

import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Result;
import cn.alphabets.light.model.datarider.DBParams;
import cn.alphabets.light.model.datarider.DataRider;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
     */
    public Plural<ModFile> add(Context handler) throws BadRequestException {


        List<Context.RequestFile> files = handler.params.getFiles();
        if (files == null) {
            throw new BadRequestException("No file content found.");
        }
        List<ModFile> result = new ArrayList<>();
        files.forEach(file -> {
            GridFSFile gfsFile = new Controller(new DBParams(handler)).writeFileToGrid(file);

            ModFile info = new ModFile();
            info.setName(gfsFile.getFilename());
            info.setLength(gfsFile.getLength());
            info.setContentType(gfsFile.getMetadata().getString("contentType"));
            info.setFileId(gfsFile.getObjectId());

            ModFile saved = DataRider.ride(ModFile.class).add(new DBParams(handler).data(info));

            //delete tmp file after upload to grid fs
            new java.io.File(file.getFilePath()).delete();

            logger.debug("file upload done : " + saved.toDocument().toJson());
            result.add(saved);
        });

        return new Plural<>((long) result.size(), result);
    }

    /**
     * Physically delete the file, and also delete the GridFS file
     *
     * @param handler context
     * @return
     * @throws LightException
     */
    public long remove(Context handler) throws LightException {

        //step 1. get meta
        ModFile info = DataRider.ride(ModFile.class).get(new DBParams(handler, true));
        if (info == null) {
            throw new BadRequestException("File not exist.");
        }
        logger.warn(info.toDocument().toString());

        //step 2. delete file from grid fs

        try {
            ObjectId fileId = info.getFileId();
            Controller ctrl = new Controller(new DBParams(handler).condition(new Document("_id", fileId)));
            ctrl.deleteFromGrid();
        } catch (Exception e) {
            logger.error("File content delete failed : ", e);
            throw new BadRequestException("File content delete failed.");
        }

        //step 3. delete file info
        return DataRider.ride(ModFile.class).remove(new DBParams(handler, true));
    }

    /**
     * Picture download
     *
     * @param handler
     * @throws LightException file not found
     */
    public void image(Context handler) throws LightException {

        try {
            ModFile file = DataRider.ride(ModFile.class).get(new DBParams(handler, true));
            Result.sendFile(handler.ctx(), file, streamForFile(handler, file));
        } catch (Exception e) {
            logger.error("error read file : ", e);
            throw new BadRequestException("File not exist.");
        }
    }


    public ByteArrayOutputStream streamForFile(Context handler, ModFile file) {
        Controller ctrl = new Controller(new DBParams(handler).condition(new Document("_id", file.getFileId())));
        return ctrl.readStreamFromGrid();
    }

    // TODO: update
    // TODO: download
    // TODO: zip
    // TODO: PDF
}
