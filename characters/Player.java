package characters;
import javax.vecmath.Vector3f;

public class Player extends Actor {

    public Player(Vector3f start,  float moveSpeed, float jumpSpeed) { 
        super(start, moveSpeed, jumpSpeed);
    }
   
}