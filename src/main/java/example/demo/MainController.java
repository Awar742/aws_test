package example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @GetMapping("/{a}")
    public String get(@PathVariable("a") double a, Model model) {
        try {
            if (a == 0) {
                model.addAttribute("response", "Введіть правильну групу");
                return "index";
            }

            HttpClient client = HttpClient.newHttpClient();

            String url = "https://be-svitlo.oe.if.ua/GavByQueue";

            Map<String, Object> jsonObject = Map.of("queue", a);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(jsonObject);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                model.addAttribute("response", "Помилка отримання відповіді: " + response.body());
                return "index";
            }

            String responseBody = response.body();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            String note = rootNode.path("current").path("note").asText("Дані відсутні");

            List<String> hourDetails = new ArrayList<>();
            JsonNode hoursListNode = rootNode.path("graphs").path("today").path("hoursList");

            for (JsonNode hourNode : hoursListNode) {
                String hour = hourNode.path("hour").asText("Невідомо");
                int electricity = hourNode.path("electricity").asInt(-1);
                hourDetails.add("hour: " + hour + ", electricity: " + electricity);
            }
            
            model.addAttribute("note", note);
            model.addAttribute("hours", hourDetails);

        } catch (Exception e) {

            model.addAttribute("response", "Сталася помилка: " + e.getMessage());
        }

        return "index";
    }
}
