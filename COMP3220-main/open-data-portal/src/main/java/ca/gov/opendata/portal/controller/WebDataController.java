package ca.gov.opendata.portal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ca.gov.opendata.portal.model.Data;
import ca.gov.opendata.portal.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller class for handling web-based data management operations.
 */
@Controller
@RequestMapping("/web/data")
public class WebDataController {

    private final DataRepository dataRepository;

    /**
     * Constructor for WebDataController.
     * @param dataRepository The DataRepository for database operations.
     */
    @Autowired
    public WebDataController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * Display a list of data entries.
     * @param model The model to add data list to.
     * @return The name of the HTML file containing the data list.
     */
    @GetMapping("/list")
    public String listData(Model model) {
        model.addAttribute("dataList", dataRepository.findAll());
        return "data-list";
    }

    /**
     * Display a form for adding new data.
     * @param model The model to add a new data object to.
     * @return The name of the HTML file containing the new data form.
     */
    @GetMapping("/new")
    public String newDataForm(Model model) {
        model.addAttribute("data", new Data());
        return "data-form";
    }

    /**
     * Process creation of new data entry.
     * @param data The data object to be created.
     * @param model The model to add success message.
     * @return The redirect path after data creation.
     */
    @PostMapping("/create")
    public String createData(@ModelAttribute Data data, Model model) {
        dataRepository.save(data);
        return "redirect:/web/data/list";
    }

    /**
     * Display a form for editing existing data.
     * @param id The ID of the data to be edited.
     * @param model The model to add the data object to.
     * @return The name of the HTML file containing the edit data form.
     */
    @GetMapping("/edit/{id}")
    public String editDataForm(@PathVariable String id, Model model) {
        Data data = dataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid data Id:" + id));
        model.addAttribute("data", data);
        return "data-form";
    }

    /**
     * Process update of existing data entry.
     * @param id The ID of the data to be updated.
     * @param data The updated data object.
     * @return The redirect path after data update.
     */
    @PostMapping("/update/{id}")
    public String updateData(@PathVariable String id, @ModelAttribute Data data) {
        dataRepository.save(data);
        return "redirect:/web/data/list";
    }

    /**
     * Process deletion of existing data entry.
     * @param id The ID of the data to be deleted.
     * @param model The model to add success message.
     * @return The redirect path after data deletion.
     */
    @GetMapping("/delete/{id}")
    public String deleteData(@PathVariable String id, Model model) {
        Data data = dataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid data Id:" + id));
        // Attempt to delete the file from the filesystem
        try {
            Path filePath = Paths.get(data.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                model.addAttribute("success", "File and data entry successfully deleted");
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to delete the file");
            // Optionally, return to a different page if file deletion is critical
            // return "redirect:/web/data/errorPage";
        }
        
        // Proceed to delete the data entry from the database
        dataRepository.delete(data);
        return "redirect:/web/data/list";
    }

    /**
     * Display details of a specific data entry.
     * @param id The ID of the data to be viewed.
     * @param model The model to add the data object and file content to.
     * @return The name of the HTML file containing the data details.
     */
    @GetMapping("/view/{id}")
    public String viewData(@PathVariable String id, Model model) {
        Data data = dataRepository.findById(id)
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid data Id:" + id));
        model.addAttribute("data", data);

        if (data.getFilePath() != null) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(data.getFilePath()));
                String fileContentString = new String(fileContent, StandardCharsets.UTF_8);
                model.addAttribute("fileContent", fileContentString);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("fileContent", "Error reading file content");
            }
        }

        return "data-details";
    }

        /**
     * Handles the upload of a file.
     * @param file The file to upload.
     * @param data The data object associated with the file.
     * @param model The model to add attributes.
     * @return The redirection path after upload completion.
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @ModelAttribute Data data, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return "redirect:/web/data/new";
        }

        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path uploadDir = Paths.get("/path/to/upload/directory/"); // Adjust path accordingly
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path filePath = uploadDir.resolve(fileName);

            if (Files.exists(filePath)) {
                model.addAttribute("error", "File with the same name already exists");
                return "redirect:/web/data/new";
            }

            Files.copy(file.getInputStream(), filePath);
            data.setFilePath(filePath.toString());
            dataRepository.save(data);

            model.addAttribute("success", "File uploaded successfully");

            // Clear the filePath attribute after successful upload
            data.setFilePath(null);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload the file");
            return "redirect:/web/data/new";
        }

        return "redirect:/web/data/list";
    }

        /**
     * Handles the download of a file.
     * @param id The ID of the data associated with the file to download.
     * @return ResponseEntity containing the file to download.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        Data data = dataRepository.findById(id)
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid data Id:" + id));
        Path filePath = Paths.get(data.getFilePath());
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(filePath); // Correctly calling Files.probeContentType(filePath)
                if (contentType == null) {
                    contentType = "application/octet-stream"; // Default content type if probeContentType doesn't determine one
                }
                return ResponseEntity.ok()
                                     .contentType(MediaType.parseMediaType(contentType))
                                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                                     .body(resource);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}