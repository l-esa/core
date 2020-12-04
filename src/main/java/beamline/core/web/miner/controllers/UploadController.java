package beamline.core.web.miner.controllers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin
public class UploadController {

	private Map<String, File> map = new HashMap<String, File>();
	
	@PostMapping(value = "/upload")
	@ResponseBody
	public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		String id = UUID.randomUUID().toString();
		
		File f = File.createTempFile("temp", "uploaded");
		try (OutputStream os = Files.newOutputStream(f.toPath())) {
			os.write(file.getBytes());
		}
		
		map.put(id, f);
		return id;
	}
	
	public File get(String id) {
		return map.get(id);
	}
}
