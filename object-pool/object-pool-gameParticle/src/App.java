import contracts.IPool;
import effects.Particle;
import pools.ParticlePool;

public class App {
    public static void main(String[] args) throws Exception {
        IPool<Particle> particlesPool = new ParticlePool(3);

        System.out.println("\n🔥 Explosão! Criando partículas...");
        var p1 = particlesPool.acquire();
        var p2 = particlesPool.acquire();
        var p3 = particlesPool.acquire();

        System.out.println("\n💨 Partículas se dissipam...");
        particlesPool.release(p1);
        particlesPool.release(p2);

        System.out.println("\n🔥 Nova explosão! Reutilizando partículas...");
        var p4 = particlesPool.acquire();
        var p5 = particlesPool.acquire();
        var p6 = particlesPool.acquire();
    }
}
