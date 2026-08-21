import java.util.logging.Logger;
import java.util.logging.Level;

/*
Resources :
    https://www.geeksforgeeks.org/java/logging-in-java/
*/

public class AppService{
    private static final Logger logger = Logger.getLogger(AppService.class.getName());

    public void processOrder(String orderID){
        logger.log(Level.INFO, "Starting processing order {0}", orderID);

        try{
            if(orderID==null){
                throw new IllegalArgumentException("Order ID cannot be null");
            }
        } catch (Exception e){
            logger.log(Level.SEVERE,"Failed to process order",e);
        }
    }
}