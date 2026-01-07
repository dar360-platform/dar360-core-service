package ae.dar360.viewing.controller;

import ae.dar360.viewing.model.Viewing;
import ae.dar360.viewing.service.ViewingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viewings")
@RequiredArgsConstructor
public class ViewingController {
    private final ViewingService viewingService;

    @GetMapping
    public ResponseEntity<List<Viewing>> getAllViewings() {
        return ResponseEntity.ok(viewingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Viewing> getViewingById(@PathVariable Long id) {
        return viewingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Viewing> createViewing(@RequestBody Viewing viewing) {
        return ResponseEntity.ok(viewingService.save(viewing));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Viewing> updateViewing(@PathVariable Long id, @RequestBody Viewing viewing) {
        return viewingService.findById(id)
                .map(existingViewing -> {
                    viewing.setId(id);
                    return ResponseEntity.ok(viewingService.save(viewing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteViewing(@PathVariable Long id) {
        viewingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
