package mef.application.controlador;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import mef.application.modelo.FileInfo;
import mef.application.modelo.RespuestaMessage;
import mef.application.service.FilesStorageService;


//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class FilesControlador {
	
	@Autowired
	FilesStorageService storageService;
	
	@PostMapping("/upload")
	public ResponseEntity<RespuestaMessage> uploadFile(@RequestParam("file") MultipartFile file) {
	    String message = "";
	    try {
	      storageService.save(file);

	      message = "Subido el archivo con exito: " + file.getOriginalFilename();
	      return ResponseEntity.status(HttpStatus.OK).body(new RespuestaMessage(message));
	    } catch (Exception e) {
	    	//e.printStackTrace();
	      message = "No se pudo cargar el archivo: " + file.getOriginalFilename() + "!";
	      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new RespuestaMessage(message));
	    }
	}

   @GetMapping("/files")
   public ResponseEntity<List<FileInfo>> getListFiles() {
      List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
    	  String filename = path.getFileName().toString();
          String url = MvcUriComponentsBuilder
          .fromMethodName(FilesControlador.class, "getFile", path.getFileName().toString()).build().toString();

          return new FileInfo(filename, url);
      }).collect(Collectors.toList());

       return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
   }

   @GetMapping("/files/{filename:.+}")
   @ResponseBody
   public ResponseEntity<Resource> getFile(@PathVariable String filename) {
	   Resource file = storageService.load(filename);
	   return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
   }

}
