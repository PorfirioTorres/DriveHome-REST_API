package com.drivehome.start.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		File f = new File(path, name);

		if (f.exists()) { // el archivo existe, cambiar su nombre
			for (int i = 1;; i++) {
				fName = nameTmp + "(" + i + ")" + extension;
				f = new File(path, fName);
				if (f.exists()) {
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
		File f;
		List<FileDrive> elements = new ArrayList<>();

		f = new File(path);
		String elementsName[] = f.list();

		for (int i = 0; i < elementsName.length; i++) {
			File tmp = new File(f.getAbsolutePath(), elementsName[i]);

			if (opc == 0) { // sacar solo los directorios
				if (tmp.isDirectory()) {
					elements.add(new FileDrive(tmp.getName(), tmp.getAbsolutePath()));
				}
			} else if (opc == 1) { // sacar solo los archivos
				if (tmp.isFile()) {
					elements.add(new FileDrive(tmp.getName(), tmp.getAbsolutePath()));
				}
			}
		}

		return elements;
	}

	@Override
	public void deleteElement(FileDrive[] elements) throws Exception {
		for (FileDrive fd: elements) {
			Path route = getPath(fd.getPath(), fd.getName());
			File f = route.toFile();
	
			if (f.isDirectory()) {
				deleteDirectory(f);
	
			} else {
				if (f.exists() && f.canRead()) {
					f.delete();
				} else {
					throw new RuntimeException("El elemento no existe o no es accesible.");
				}
			}
		}

	}

	private void deleteDirectory(File file) throws Exception {
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					deleteDirectory(entry);
				}
			}
		}
		
		if (!file.delete()) {
			throw new RuntimeException("Algo falló al eliminar el elemento " + file);			
		}
	}

	@Override
	public Boolean createDir(String path, String name) throws Exception {
		File dir = new File(path, name);
		
		if (dir.exists()) {
			return false;
		} else {
			dir.mkdir();
			return true;
		}
	}

	@Override
	public Boolean renameElement(String path, String oldName, String newName) throws Exception {
		File original = new File(path, oldName);
		Path source = getPath(path, oldName);
		
		if (original.exists()) {
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
			resource = null;
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
			File f = new File(fd.getPath(), fd.getName());
			Path route = getPath(fd.getPath(), fd.getName());
			
			if (f.isDirectory()) {	// es una carpeta
				resources = null;
				resources = resourcesLoader(route.toString());
				if (resources != null && resources.length > 0) {
					for (Resource resource: resources) {
						resourcesTmp.add(resource);
					}
				}
			} else if (f.isFile()) { // es un archivo
				Resource resource = new UrlResource(route.toUri());
				resourcesTmp.add(resource);
			}
		}
		
		resources = new Resource[resourcesTmp.size()];
		resources = resourcesTmp.toArray(resources);
		return resources;
		
	}
	
}
