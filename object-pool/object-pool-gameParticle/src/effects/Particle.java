package effects;

public class Particle {
  private boolean isActive;

  public Particle(){
    this.isActive = false;
  }

  public void turnOn(){
    this.isActive = true;
  }

  public void turnOff(){
    this.isActive = false;
  }

  public boolean isActive(){
    return isActive;
  }
}
