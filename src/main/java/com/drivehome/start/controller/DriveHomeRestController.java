package com.drivehome.start.controller;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drivehome.start.model.FileDrive;
import com.drivehome.start.service.IDriveHomeService;
import com.drivehome.start.util.Utils;
import com.drivehome.start.util.Validations;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value="/files")
public class DriveHomeRestController {
	@Autowired
	private IDriveHomeService driveHomeService;
	
	@PostMapping(value="/upload")
	public ResponseEntity<?> upload(@RequestParam ("files") MultipartFile[] files, @RequestParam String path) {
		ResponseEntity<?> rEntity = null;
		Map<String, Object> response = new HashMap<>();
		HttpStatus resphttp;
		
		
		try {
			path = Validations.sanitizePath(path);
			
			if (!Validations.validatePath(path)) {
				response.put("message", "El path no es válido.");
				resphttp = HttpStatus.BAD_REQUEST;
			} else {
				if (files.length == 0) {
					response.put("message", "No hay archivos para subir.");
					resphttp = HttpStatus.BAD_REQUEST;
				} else {
					driveHomeService.uploadFiles(files, path);
					response.put("success", "Archivos subidos exitosamente.");
					resphttp = HttpStatus.CREATED;
					
				}
			}
			rEntity = new ResponseEntity<Map<String, Object>>(response, resphttp);
		} catch (Exception e) {
			System.out.println(e.getClass() + "\n" +e.getMessage());
			
			response.put("message", "Ocurrió un error al subir los archivos. Intente más tarde.");
			rEntity = new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return rEntity;
	}
	
	@GetMapping(value="/getfiles")
	public ResponseEntity<?> getFiles(@RequestParam ("path") String path) {
		ResponseEntity<?> rEntity = null;
		Map<String, Object> response = new HashMap<>();
		HttpStatus resphttp;
		
		try {
			path = Validations.sanitizePath(path);
			
			if (!Validations.validatePath(path)) {
				response.put("message", "El path no es válido.");
				resphttp = HttpStatus.BAD_REQUEST;
			} else {
				response.put("files", driveHomeService.getDirectoriesAndFiles(path).get("files"));
				response.put("directories", driveHomeService.getDirectoriesAndFiles(path).get("directories"));
				resphttp = HttpStatus.OK;
			}
			
			rEntity = new ResponseEntity<Map<String, Object>>(response, resphttp);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			
			response.put("message", e.getMessage());
			rEntity = new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return rEntity;
	}
	
	@DeleteMapping(value="/delete")
	public ResponseEntity<?> delete(@RequestBody FileDrive[] elements) {
		ResponseEntity<?> rEntity = null;
		Map<String, Object> response = new HashMap<>();
		HttpStatus resphttp;
		
		try {
			
			if (elements.length == 0) {
				response.put("message", "Los parametros enviados no son válidos, verifique.");
				resphttp = HttpStatus.BAD_REQUEST;
			} else {
				driveHomeService.deleteElement(elements);
				response.put("success", "Elemento/s eliminado/s con éxito.");
				resphttp = HttpStatus.OK;
				
			}
			
			
			rEntity = new ResponseEntity<Map<String, Object>>(response, resphttp);
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
			
			response.put("message", e.getMessage());
			rEntity = new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return rEntity;
	}
	
	@PostMapping(value="/createdir")
	public ResponseEntity<?> createDir(@RequestParam String path, @RequestParam String name) {
		ResponseEntity<?> rEntity = null;
		Map<String, Object> response = new HashMap<>();
		HttpStatus resphttp;
		
		try {
			path = Validations.sanitizePath(path);
			
			if (!Validations.validatePath(path) || !Validations.validateName(name)) {
				response.put("message", "El path y/o el nombre del elemento no es válido.");
				resphttp = HttpStatus.BAD_REQUEST;
			} else {
				if (driveHomeService.createDir(path, name)) {
					response.put("success", "Directorio " + name + " creado con éxito.");
					resphttp = HttpStatus.CREATED;
				} else {
					response.put("message", "El directorio ya existe.");
					resphttp = HttpStatus.BAD_REQUEST;
				}
			}
			
			rEntity = new ResponseEntity<Map<String, Object>>(response, resphttp);
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
			
			response.put("message", "Ha ocurrido un error.");
			rEntity = new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return rEntity;
	}
	
	@PutMapping(value="/rename")
	public ResponseEntity<?> rename(@RequestParam String path, @RequestParam String oldName, @RequestParam String newName) {
		ResponseEntity<?> rEntity = null;
		Map<String, Object> response = new HashMap<>();
		HttpStatus resphttp;
				
		try {
			path = Validations.sanitizePath(path);
			
			if (!Validations.validatePath(path) || !Validations.validateName(oldName) || !Validations.validateName(newName)) {
				response.put("message", "El path y/o los valores para renombrar no son válidos.");
				resphttp = HttpStatus.BAD_REQUEST;
			} else {
				if (driveHomeService.renameElement(path, oldName, newName)) {
					response.put("success", "El elemento ha sido renombrado.");
					resphttp = HttpStatus.CREATED;
				} else {
					response.put("message", "El elemento que desea renombrar no existe.");
					resphttp = HttpStatus.NOT_FOUND;
				}
			}
			rEntity = new ResponseEntity<Map<String, Object>>(response, resphttp);
			
		} catch(Exception e) {
			if (e instanceof FileAlreadyExistsException) {
				response.put("message", "Ya existe un archivo con el nombre " + newName);
			} else {				
				System.out.println(e.getClass() + " " +e.getMessage());
				response.put("message", "Ha ocurrido un error.");
			}
			
			rEntity = new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return rEntity;
	}
	
	@PostMapping(value="/download", produces="application/zip")
	public void downloadFiles(@RequestBody FileDrive[] files, HttpServletResponse sresponse) {
		if (files.length == 0) {
			sresponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			try {
				Resource[] resources = driveHomeService.download(files);
				
				if (resources.length > 0) {
					
					ZipOutputStream zos = new ZipOutputStream(sresponse.getOutputStream());
					
					for (Resource r: resources) {
						System.out.println(r);
						ZipEntry zipEntry = new ZipEntry(r.getFilename());
						zipEntry.setSize(r.contentLength());
						zos.putNextEntry(zipEntry);
						StreamUtils.copy(r.getInputStream(), zos);
						zos.closeEntry();
					}
					zos.finish();
					zos.close();
					String zipName = Utils.generateName() + ".zip";
				   
					sresponse.setStatus(HttpServletResponse.SC_OK);
					sresponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\"");
					
				} else {
					sresponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
					
				}
			} catch (Exception e) {
				System.out.println(e.getClass() + " " +e.getMessage());
				sresponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
			}
		}
	}
	
	@GetMapping(value="/download/directory", produces="application/zip")
	public void downloadFolder(@RequestParam String path, @RequestParam String folder, HttpServletResponse sresponse) {
		if (folder == null || !Validations.validateName(folder) || !Validations.validatePath(path)) {
			sresponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			try {
				Resource[] resources = driveHomeService.getResourcesFolder(path, folder);
				
				if (resources.length > 0) {
					
					ZipOutputStream zos = new ZipOutputStream(sresponse.getOutputStream());
					
					Path src = Paths.get(path);
					for (Resource r: resources) {
						System.out.println(r);
						String name = src.relativize(Paths.get(r.getFile().getAbsolutePath())).toString().replace("\\", "/");
						if (Files.isDirectory(Paths.get(r.getFile().getAbsolutePath()))) {
							name += "/";
						}
						
						ZipEntry zipEntry = new ZipEntry(name);
						zos.putNextEntry(zipEntry);
						
						if (!Files.isDirectory(Paths.get(r.getFile().getAbsolutePath()))) {
							Files.copy(Paths.get(r.getFile().getAbsolutePath()), zos);
						}
						
						zos.closeEntry();
					}
					zos.finish();
					zos.close();
					String zipName = Utils.generateName() + ".zip";
				   
					sresponse.setStatus(HttpServletResponse.SC_OK);
					sresponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\"");
					
				} else {
					sresponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
					
				}
			} catch (Exception e) {
				System.out.println(e.getClass() + " " +e.getMessage());
				sresponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
			}
		}
	}
	
	@GetMapping(value="/download/single/{file:.+}") // con :.+ se indica que el nombre contiene un punto y mas caracteres despues
	public ResponseEntity<Resource> verFoto(@PathVariable String file) throws Exception {
		ResponseEntity<Resource> responce = null;
		try {
			FileDrive[] fdrive = {new FileDrive(file, "c:/cloud")};
			
			Resource[] resource = driveHomeService.download(fdrive);
			System.out.println(resource[0]);
			// forzar descarga del recurso
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource[0].getFilename() + "\"");
			
			responce = new ResponseEntity<Resource>(resource[0], headers, HttpStatus.OK);
		} catch (MalformedURLException e) {
			System.out.println("---" + responce);
			e.printStackTrace();
		}
		return responce;
	}
}
