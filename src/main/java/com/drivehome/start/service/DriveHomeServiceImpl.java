package com.drivehome.start.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drivehome.start.model.FileDrive;

@Service
public class DriveHomeServiceImpl implements IDriveHomeService {
	private ResourceLoader resourceLoader;
	
	@Autowired
	public DriveHomeServiceImpl(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	@Override
	public void uploadDirectory(MultipartFile[] files, String path) throws Exception {
		// verificar si ya existe el directorio
		String[] base = files[0].getOriginalFilename().split("/");
		String dirbase = base[0];
		
		Path basePath = getPath(path, dirbase);
		
		if (Files.exists(basePath)) {
			throw new RuntimeException("Ya existe un directorio con ese nombre.");
		}
		// crear el arbol de directorios
		String[] routes = getSubPaths(files);
		if (routes != null && routes.length > 0) {
			for (int i = 0; i < routes.length; i++) {				
				Files.createDirectories(getPath(path, routes[i]));
			}
			
			uploadFiles(files, path);
		}
		
	}
	
	private String[] getSubPaths(MultipartFile[] files) {
		String[] routes = new String[files.length];
		// generar el arbol de directorios
		for (int f = 0; f < files.length; f++) {
			String[] resource = files[f].getOriginalFilename().split("/");
			StringBuilder tree = new StringBuilder();
			
			for (int i = 0; i < resource.length-1; i++) {
				tree.append(resource[i]);
				tree.append("/");
			}
			routes[f] = tree.toString();
		}
		
		return routes;
	}

	@Override
	public void uploadFiles(MultipartFile[] files, String path) throws Exception {
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				// obtener y procesar nombre del archivo
				String nameFile = file.getOriginalFilename();
				nameFile = finalName(path, nameFile);
				// System.out.println(nameFile);

				Path route = getPath(path, nameFile); // obtener ruta de almacenaje, incluye el nombre del archivo
				// System.out.println(route.toString());

				Files.copy(file.getInputStream(), route); // subirlo
			}

		}
	}

	private Path getPath(String path, String nameFile) throws Exception {
		return Paths.get(path).resolve(nameFile).toAbsolutePath();
	}

	private String finalName(String path, String name) throws Exception {
		String extension = name.substring(name.lastIndexOf(".")); // extension
		String nameTmp = name.substring(0, name.lastIndexOf(".")); // nombre sin extension
		String fName;
		
		Path pathFile = getPath(path, name);
		
		if (Files.exists(pathFile)) {
			for (int i = 1;; i++) {
				fName = nameTmp + "(" + i + ")" + extension;
				pathFile = getPath(path, fName);
				if (Files.exists(pathFile)) {
					continue;
				} else {
					break;
				}
			}
		} else { // no existe el archivo, usar el actual
			fName = name;
		}

		return fName;
	}

	@Override
	public Map<String, Object> getDirectoriesAndFiles(String path) throws Exception {
		Map<String, Object> elements = new HashMap<>();
		List<FileDrive> directories = getElements(path, 0);
		List<FileDrive> files = getElements(path, 1);

		elements.put("directories", directories);
		elements.put("files", files);

		return elements;
	}

	private List<FileDrive> getElements(String path, int opc) throws Exception {
		List<FileDrive> elements = new ArrayList<>();
		
		Path pathDir = Paths.get(path);
		Stream<Path> paths = Files.list(pathDir);
		
		paths.forEach(pathTmp -> {
			FileDrive fd = new FileDrive(pathTmp.getFileName().toString(), path);
			if (opc == 0) {
				if (Files.isDirectory(pathTmp)) {
					elements.add(fd);
				}
			} else if (opc == 1) {
				if (!Files.isDirectory(pathTmp)) {
					elements.add(fd);
				}
					
			}
		});
		paths.close();

		return elements;
	}

	@Override
	public void deleteElement(FileDrive[] elements) throws Exception {
		for (FileDrive fd: elements) {
			Path path = getPath(fd.getPath(), fd.getName());
			
			if (Files.exists(path)) {
				if (Files.isDirectory(path)) {
					deleteDirectory(path);
				} else {
					if (!Files.deleteIfExists(path)) {
						throw new RuntimeException("No se pudo borrar el archivo " + path.getFileName().toString());
					}
				}
			}
			
		}

	}
	
	private void deleteDirectory(Path path) throws Exception {
		try {
			Files.walk(path)
			.sorted(Comparator.reverseOrder())
			.forEach(pathTmp -> {
				
					try {
						Files.deleteIfExists(pathTmp);
					} catch (IOException e) {
						throw new RuntimeException("Ocurrió un error al eliminar " + pathTmp);
					}
				
			});
		} catch (Exception e) {
			throw new Exception (e);
		}
		
	}

	@Override
	public Boolean createDir(String path, String name) throws Exception {
		Path pathdir = getPath(path, name);
		
		if (!Files.exists(pathdir)) {
			Files.createDirectory(pathdir);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean renameElement(String path, String oldName, String newName) throws Exception {
		Path source = getPath(path, oldName);
		
		if (Files.exists(source)) {
			Files.move(source, source.resolveSibling(newName));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Resource download(FileDrive file) throws Exception {
		Resource resource = null;
		
		Path route = getPath(file.getPath(), file.getName());
		resource = new UrlResource(route.toUri());
			
		if (!resource.exists() || !resource.isReadable()) {
			System.out.println ("No se puede acceder al recurso: " + file.getName());
			throw new RuntimeException("No se puede acceder al recurso: " + file.getName());
		}
		
		return resource;
	}
	
	private Resource[] resourcesLoader(String path) throws Exception {
		return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("file:" + path +"/**");
	}

	@Override
	public Resource[] obtainResources(List<FileDrive> files) throws Exception {
		Resource[] resources = null;
		List<Resource> resourcesTmp = new ArrayList<>();
		
		for (FileDrive fd: files) { 
			Path path = getPath(fd.getPath(), fd.getName());
			
			if (Files.isDirectory(path)) {	// es una carpeta
				resources = null;
				resources = resourcesLoader(path.toString());
				if (resources != null && resources.length > 0) {
					for (Resource resource: resources) {
						resourcesTmp.add(resource);
					}
				}
			} else { // es un archivo
				Resource resource = new UrlResource(path.toUri());
				resourcesTmp.add(resource);
			}
		}
		
		resources = new Resource[resourcesTmp.size()];
		resources = resourcesTmp.toArray(resources);
		return resources;
		
	}
	
}
