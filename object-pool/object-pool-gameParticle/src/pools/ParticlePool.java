package pools;

import java.util.ArrayList;
import java.util.Collection;

import contracts.IPool;
import effects.Particle;

public class ParticlePool implements IPool<Particle> {
  private Collection<Particle> particles;

  public ParticlePool(int qtd){
    this.particles = new ArrayList<>();
    for(int i = 0; i < qtd; i++){
      particles.add(new Particle());
    }
  }

  @Override
  public Particle acquire() {
    for(Particle particle : particles) {
      if(!particle.isActive()) {
        particle.turnOn();
        return particle;
      }
    }
    System.out.println("Nenhuma partícula livre! Criando nova");
    var newParticle = new Particle();
    newParticle.turnOn();
    particles.add(newParticle);
    return newParticle;
  }

  @Override
  public void release(Particle particle) {
    particle.turnOff();
  }

}
