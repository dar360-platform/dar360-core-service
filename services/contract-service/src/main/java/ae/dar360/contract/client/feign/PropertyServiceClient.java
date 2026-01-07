package ae.dar360.contract.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "property-service", url = "${services.property.url}")
public interface PropertyServiceClient {

    @GetMapping("/api/properties/{id}")
    PropertyDTO getPropertyById(@PathVariable Long id);

    class PropertyDTO {
        private Long id;
        private String title;
        // other fields as needed
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
