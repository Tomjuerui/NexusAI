package com.moyz.nexus.chat.controller;

import com.moyz.nexus.common.entity.NexusFile;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.file.FileOperatorContext;
import com.moyz.nexus.common.file.LocalFileUtil;
import com.moyz.nexus.common.service.FileService;
import com.moyz.nexus.common.util.UrlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.moyz.nexus.common.cosntant.NexusConstant.IMAGE_EXTENSIONS;
import static com.moyz.nexus.common.enums.ErrorEnum.A_FILE_NOT_EXIST;
import static com.moyz.nexus.common.enums.ErrorEnum.B_IMAGE_LOAD_ERROR;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;

@Slf4j
@RestController
@Validated
public class FileController {

    //缓存一�?
    private static final String CACHE_TIME = "public, max-age=31536000";

    @Resource
    private FileService fileService;

//    @Operation(summary = "我的图片")
//    @GetMapping(value = "/my-image/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
//    public void myImage(@Length(min = 32, max = 32) @PathVariable String uuid, HttpServletResponse response) {
//        NexusFile NexusFile = fileService.getByUuid(uuid);
//        if (null == NexusFile) {
//            throw new BaseException(A_FILE_NOT_EXIST);
//        }
//        responseImage(uuid, NexusFile.getExt(), false, response);
//    }

    @GetMapping(value = "/my-thumbnail/{uuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void thumbnail(@Length(min = 32) @PathVariable String uuidWithExt, HttpServletResponse response) {
        String uuid = UrlUtil.getUuid(uuidWithExt);
        NexusFile NexusFile = fileService.getByUuid(uuid);
        if (null == NexusFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        responseImage(uuid, NexusFile.getExt(), true, response);
    }

//    /**
//     * 获取图片
//     *
//     * @param uuid     图片uuid
//     * @param response HttpServletResponse
//     */
//    @GetMapping(value = "/image/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
//    public void image(@Length(min = 32, max = 32) @PathVariable String uuid, HttpServletResponse response) {
//        NexusFile NexusFile = fileService.getByUuid(uuid);
//        if (null == NexusFile) {
//            throw new BaseException(A_FILE_NOT_EXIST);
//        }
//        responseImage(uuid, NexusFile.getExt(), false, response);
//    }

    @GetMapping(value = "/file/{uuidWithExt}")
    public ResponseEntity<org.springframework.core.io.Resource> file(@Length(min = 32) @PathVariable String uuidWithExt, HttpServletResponse response) {
        String uuid = UrlUtil.getUuid(uuidWithExt);
        NexusFile NexusFile = fileService.getByUuid(uuid);
        if (null == NexusFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        if (IMAGE_EXTENSIONS.contains(NexusFile.getExt().toLowerCase())) {
            responseImage(uuid, NexusFile.getExt(), false, response);
            return null;
        }
        response.setHeader(CACHE_CONTROL, CACHE_TIME);
        byte[] bytes = LocalFileUtil.readBytes(NexusFile.getPath());
        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));

        String fileName = NexusFile.getName();
        if (StringUtils.isBlank(fileName)) {
            fileName = NexusFile.getUuid() + "." + NexusFile.getExt();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }

    private void responseImage(String uuid, String ext, boolean thumbnail, HttpServletResponse response) {
        BufferedImage bufferedImage = fileService.readMyImage(uuid, thumbnail);
        //把图片写给浏览器
        try {
            // 缓存30�?
            response.setHeader(CACHE_CONTROL, CACHE_TIME);
            ImageIO.write(bufferedImage, ext, response.getOutputStream());
        } catch (IOException e) {
            log.error("image error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    @PostMapping(path = "/file/upload", headers = "content-type=multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> upload(@RequestPart(value = "file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        NexusFile NexusFile = fileService.saveFile(file, false);
        result.put("uuid", NexusFile.getUuid());
        result.put("url", FileOperatorContext.getFileUrl(NexusFile));
        return result;
    }

    @PostMapping(path = "/image/upload", headers = "content-type=multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> imageUpload(@RequestPart(value = "file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        NexusFile NexusFile = fileService.saveFile(file, true);
        result.put("uuid", NexusFile.getUuid());
        result.put("url", FileOperatorContext.getFileUrl(NexusFile));
        return result;
    }

    @PostMapping("/file/del/{uuid}")
    public boolean del(@PathVariable String uuid) {
        return fileService.removeFileAndSoftDel(uuid);
    }
}
