package com.example.genai.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiImageModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:63343")
public class OpenAiRestController {

   private final AzureOpenAiChatModel chatModel;
   private final AzureOpenAiImageModel imageModel;
    private static final Logger logger = LogManager.getLogger(OpenAiRestController.class);

    public OpenAiRestController(AzureOpenAiChatModel chatModel, AzureOpenAiImageModel imageModel) {
        this.chatModel = chatModel;
        this.imageModel = imageModel;
    }

@GetMapping("/recipechat")
public String recipeGeneration(@RequestParam String message){
    // Generate an image based on the recipe name
    ImageResponse imageResponse = imageModel.call(new ImagePrompt(message + " image"));
    String imageUrl = imageResponse.getResult().getOutput().getUrl();

    // Create a UserMessage with the user's message
    UserMessage userMessage = new UserMessage(message);

    // Create a SystemMessage
    SystemMessage systemMessage = new SystemMessage("You are a master chef. I would like you to provide the ingredients, steps, and duration of making a recipe. The recipe name is '" + message + "'. Please provide the information in the following JSON format: {\"ingredients\": [\"ingredient1\", \"ingredient2\"], \"steps\": [\"step1\", \"step2\"],\"duration\":\"MM:00\"}. Please note that the ingredients and steps are just examples, replace them with the actual ingredients and steps for the recipe.");

    // Call the chat model with the UserMessage and SystemMessage
    ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage)));

    // Return the content of the chat model's response
    String output = response.getResult().getOutput().getContent();
    JsonObject jsonObject = JsonParser.parseString(output)
            .getAsJsonObject();
    jsonObject.addProperty("image", imageUrl);
    return jsonObject.toString();
}






}
