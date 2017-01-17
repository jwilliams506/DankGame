package statics;

import javax.vecmath.Vector3f;

public class GameObject {
    private Vector3f position = new Vector3f(); // controls position of the character
    
    public GameObject(Vector3f start) {
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
}
