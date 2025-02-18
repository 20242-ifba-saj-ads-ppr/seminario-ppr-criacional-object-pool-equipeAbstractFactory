# Object Pool (Padrão Criacional)

## Intenção

Gerenciar a criação, armazenamento, emprestimo, retomada e reutilização de instancias de objeto, com o objetivo de controlar a quantidade de instancias existentes ou previnir o processo de criação e destruição recorentes quando estes forem considerados caros.

## Também conhecido como

Pool de recursos

## Motivação

Para se comunicar com um banco de dados, é necessario estabelecer uma "conexão" com ele:

```java
Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/meu_banco",
                "usuario",
                "senha" )

Statement statement = connection.createStatement();

ResultSet resultSet = statement.executeQuery("SELECT id, nome FROM clientes");
```

Em uma aplicação como um sistema web, onde varias requisições chegam o tempo todo, e para cada requisição é comum termos que acessar o banco de dados uma ou mais vezes, nesse caso, para cada acesso precisariamos instanciar a conexão.

```plantuml
@startuml
actor Client
participant "Web Server" as Server
participant "Database" as DB

Client -> Server: HTTP Request
Server -> Server: Instantiate Connection
Server -> DB: Query Data
DB --> Server: Return Data
Server -> Server: Destroy Connection
Server --> Client: HTTP Response

@enduml
```
![FluxoSemPool](./images/FluxoSemPool.png)

Isso rapidamente apresenta um problema, estabelecer uma conexão com banco de dados é um processo relativamente caro e demorado, é necessario a realização de diversas etapas tanto no servidor de banco quanto no cliente que está se conectando. Além disso, servidores de banco de dados possuem um numero maximo de conexões simultaneas que ele pode manter.

Em um cenario em que por exemplo uma aplicação receba 1000 requisições/s, e para cada requisição sejam necessarias em media 2 consultas ao banco de dados, isso significa que estariamos instanciando e destruindo 2000 conexões por segundo, um numero que facilmente extrapolaria o limite de conexões de um banco de dados.

Em vez disso, usando o pattern de object pool, podemos implementar uma classe que sirva como pool de conexões, dessa forma, ao precisarmos de uma conexão solicitamos ao pool, que ira nos fornecer uma conexão já existente que foi inicializada com o pool. Ao terminamos de usar a conexão, devolvemos ela ao pool.


```plantuml
@startuml
actor Client
participant "Web Server Endpoint" as Server
participant "Connection Pool" as Pool
participant "Database" as DB

Pool -> DB: Connection Pool Initialization
DB --> Pool: Connection Instances
Client -> Server: HTTP Request
Server -> Pool: Request Connection
Pool --> Server: Provide Connection
Server -> DB: Query Data
DB --> Server: Return Data
Server -> Pool: Return Connection
Server --> Client: HTTP Response

@enduml
```
![FluxoSemPool](./images/FluxoComPool.png)


## Aplicabilidade

Use object pool quando:

- For **demorado** criar uma instancia
- For **caro** em recursos criar uma instancia
- For **demorado** destruir uma instancia
- For **caro** em recursos destruir uma instancia

- Existe um **limite** de quantas instancias possam existir simultaneamente

Com **recursos**, queremos dizer por exemplo cpu, ram, disco e rede por exemplo

Não use object pool quando:

- O **custo** de **manter** a instancia, mesmo quando não está sendo usada, supera o custo de instanciala.

## Estrutura


```plantuml
@startuml
left to right direction
interface PoolInterface {
    + acquire(): Object
    + release(Object): void
}

interface PooledObjectFactoryInterface {
    + createObject(): Object
}

class PoolConcrete implements PoolInterface {
    - PooledObjectFactoryInterface factory
    + acquire(): Object
    + release(Object): void
}

class ObjectFactoryConcrete implements PooledObjectFactoryInterface {
    + createObject(): Object
}

class Client {
    - PoolInterface pool
    + usePool(): void
}

PoolConcrete --> ObjectFactoryInterface : uses
Client --> PoolInterface : interacts with
@enduml
```
![Texto alternativo](./images/Estrutura.png)

## Participantes

- **PoolInterface**
    - Define uma interface comum para todas as implementações de classes de pool de objetos
- **ObjectFactoryInterface** 
    - Define uma interface comum para todas as implementações de classes fabricas de objetos que seram guardadas em pool.
- **Client**
   - Aquele que necessita das instancias do objeto que sera guardado em pool.

## Implementação

- Implementação de um pool

```java
package com.example.implementations.simple;

import java.util.ArrayList;
import java.util.List;

import com.example.interfaces.PoolInterface;
import com.example.interfaces.PooledObjectFactory;

public class SimplePool<T> implements PoolInterface<T> {
    private List<T> instanciasLivres; 
    private List<T> instanciasEmUso;  
    private PooledObjectFactory<T> factory;

    public SimplePool(int size, PooledObjectFactory<T> factory) {
        this.factory = factory;
        this.instanciasLivres = new ArrayList<>();
        this.instanciasEmUso = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            instanciasLivres.add(factory.create());
        }
    }

    @Override
    public T acquire() {
        if (!instanciasLivres.isEmpty()) {
            T instance = instanciasLivres.remove(0);  
            instanciasEmUso.add(instance);  
            return instance;
        }
        return null;
    }

    @Override
    public void release(T instance) {
        if (instanciasEmUso.remove(instance)) {
            instanciasLivres.add(instance);
        }
    }

    @Override
    public void destroyAll() {
        for (T instance : instanciasLivres) {
            factory.destroy(instance);
        }
        for (T instance : instanciasEmUso) {
            factory.destroy(instance);
        }
    }
}
```

## Exemplo de código

- Uso de um pool

```java
PoolInterface<CheapObject> pool = new SimplePool<CheapObject>(1, new CheapObjectFactory());

        for (int i = 0; i < 100; i++) {
            CheapObject cheapObject = pool.acquire();
            cheapObject.doSomething();
            pool.release(cheapObject);
        }
```

## Usos conhecidos

- Conexões com bancos de dados geralmente são gerenciados por um object pool

- Servidores web e de aplicação implementam um pool de threads para o processamento de requisições

- Em aplicações multithreads, threads de trabalho são gerenciadas por um object pool

## Padrão relacionados


## Referências


## Outros Exemplos de Código

### Exemplo prático: Pool de particulas em um jogo

Considere a seguinte situação: estamos desenvolvendo um jogo onde explosões e efeitos de fogo ocorrem frequentemente. Cada vez que uma explosão acontece, dezenas ou centenas de partículas de fogo são geradas para criar um efeito visual realista.

Se criarmos e destruirmos essas partículas dinamicamente toda vez que uma explosão ocorre, o jogo sofrerá quedas de desempenho devido ao custo de alocação de memória e garbage collection.

Uma abordagem mais eficiente seria reutilizar as partículas, evitando recriação constante. Podemos manter um pool de partículas que são ativadas e desativadas conforme necessário.

Dessa forma, melhoramos a eficiência do jogo, reduzindo o consumo de memória e o tempo de processamento.



### UML do Object Pool 

```mermaid
classDiagram
    direction TB

    class IPool {
        +acquire() T
        +release(t: T) void
    }

    class Particle {
        -isActive: boolean
        +Particle()
        +turnOn() void
        +turnOff() void
        +isActive() boolean
    }

    class ParticlePool {
        -particles: Collection~Particle~
        +ParticlePool(qtd: int)
        +acquire() Particle
        +release(particle: Particle) void
    }

    class App {
    }

    IPool <|-- ParticlePool
    ParticlePool o-- Particle
    App --> ParticlePool

```

### Código Java do Object Pool:

#### Interface Pool
@import "./object-pool-gameParticle/src/contracts/IPool.java"

#### Classe Particle (Objeto a ser reutilizado)
@import "./object-pool-gameParticle/src/effects/Particle.java"

#### Classe ParticlePool (Gerenciador do Pool)
@import "./object-pool-gameParticle/src/pools/ParticlePool.java"

#### Classe Main (Cliente)
@import "./object-pool-gameParticle/src/App.java"


#### Explicação do Código
1. Criamos a classe Particle, que representa uma partícula de fogo e pode ser ativada (turnOn()) ou desativada (turnOff()).
2. Criamos a interface IPool, que define os métodos acquire() e release() para controle de recursos.
3. Criamos a classe ParticlePool, que gerencia uma lista de partículas disponíveis e as reutiliza quando possível.
4. No método acquire(), o pool fornece uma partícula inativa, evitando criar uma nova instância desnecessariamente.
5. No método release(), a partícula é desativada e devolvida ao pool para ser reutilizada futuramente.
6. Na classe App, simulamos um jogo em que partículas são usadas para explosões e depois reutilizadas, reduzindo o impacto no desempenho.

### Participantes
- **Product (Particle)**
Define os objetos gerenciados pelo pool. Neste caso, Particle representa uma partícula gráfica que pode ser ativada ou desativada.
- **Pool (IPool)**
Interface que define os métodos para aquisição (acquire()) e liberação (release()) dos objetos gerenciados.
- **ConcretePool (ParticlePool)**
Implementação do IPool, responsável por gerenciar um conjunto de Particle e otimizar seu uso reutilizando-as em vez de criar novas instâncias.
