package dtb.user.print;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

@SpringBootApplication
public class UserPrintSpringApplication {

	public static ConfigurableApplicationContext context;
	
	public static void main(String[] args) {
		context = SpringApplication.run(UserPrintSpringApplication.class, args);
		
		((AbstractApplicationContext) context).registerShutdownHook();
		
		
	}
	
	public static void stopApplication(){
		context.close();
	}

}
