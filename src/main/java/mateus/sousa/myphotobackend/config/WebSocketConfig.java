package mateus.sousa.myphotobackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import mateus.sousa.myphotobackend.websocket.PhotoHandler;;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("Creating WebSocket Listener");        
        registry.addHandler(photoHandler(), "/").setAllowedOrigins("*");
    }
    
    @Bean
    public PhotoHandler photoHandler() {
        return new PhotoHandler();
    }
}
