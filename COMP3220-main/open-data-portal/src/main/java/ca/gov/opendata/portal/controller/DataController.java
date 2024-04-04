package ca.gov.opendata.portal.controller;

import ca.gov.opendata.portal.model.Data;
import ca.gov.opendata.portal.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for handling CRUD operations related to data entities.
 */
@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataRepository dataRepository;

    /**
     * Constructor for DataController.
     * @param dataRepository The DataRepository to be used for data operations.
     */
    @Autowired
    public DataController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * Create a new dataset.
     * @param data The data object to be created.
     * @return ResponseEntity with the created Data object.
     */
    @PostMapping
    public ResponseEntity<Data> createData(@RequestBody Data data) {
        Data savedData = dataRepository.save(data);
        return ResponseEntity.ok(savedData);
    }

    /**
     * Get all datasets.
     * @return ResponseEntity with a list of all Data objects.
     */
    @GetMapping
    public ResponseEntity<List<Data>> getAllData() {
        List<Data> dataList = dataRepository.findAll();
        return ResponseEntity.ok(dataList);
    }

    /**
     * Get a single dataset by ID.
     * @param id The ID of the dataset to retrieve.
     * @return ResponseEntity with the retrieved Data object.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Data> getDataById(@PathVariable String id) {
        return dataRepository.findById(id)
                .map(data -> ResponseEntity.ok(data))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a dataset.
     * @param id The ID of the dataset to update.
     * @param newData The updated data object.
     * @return ResponseEntity with the updated Data object.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Data> updateData(@PathVariable String id, @RequestBody Data newData) {
        return dataRepository.findById(id)
                .map(data -> {
                    data.setTitle(newData.getTitle());
                    data.setDescription(newData.getDescription());
                    data.setFilePath(newData.getFilePath());
                    data.setOwnerId(newData.getOwnerId());
                    Data updatedData = dataRepository.save(data);
                    return ResponseEntity.ok(updatedData);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a dataset.
     * @param id The ID of the dataset to delete.
     * @return ResponseEntity indicating success or failure of deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteData(@PathVariable String id) {
        return dataRepository.findById(id)
                .map(data -> {
                    dataRepository.delete(data);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
