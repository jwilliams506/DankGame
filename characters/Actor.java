package characters;

import javax.vecmath.Vector3f;

public class Actor {
    private Vector3f position = new Vector3f(); // controls position of the character
    private float velX;  // controls the character x velocity
    private float velY;  // controls the character y velocity
    private float gravity = .01f;  // controls the gravity of the character
    private float moveSpeed = .05f;
    private float jumpSpeed = .4f;
    
    public Actor(Vector3f start, float moveSpeed, float jumpSpeed) { // sets the initial position of character 
        this.position.x = start.x;
        this.position.y = start.y;
        this.position.z = start.z;
        this.moveSpeed = moveSpeed;
        this.jumpSpeed = jumpSpeed;
    }
    
    public Actor(Vector3f start) {
        this.position.x = start.x;
        this.position.y = start.y;
        this.position.z = start.z;
    }
    
    public float getX() {
        return position.x;
    }    
    
    public void setX(float x) {
        position.x = x;
    }

    public float getY() {
        return position.y;
    }

    public void setY(float y) {
        position.y = y;
    }
    
    public float getZ() {
        return position.z;
    }

    public void setZ(float z) {
        position.z = z;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }    
    
    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    } 
}
