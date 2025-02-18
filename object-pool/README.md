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

### Exemplo prático: Pool de conexões com banco de dados

Imagine que temos um sistema que precisa abrir muitas conexões com o banco de dados.
Criar e destruir uma conexão toda vez seria lento e ineficiente.
Para resolver isso, usamos um Object Pool que mantém algumas conexões prontas para uso.
Quando um cliente precisa de uma conexão, ele pega do pool. Quando termina, ele devolve, em vez de destruir.

### UML do Object Pool 

```mermaid
classDiagram
    class Conexao {
        + usar()
        + liberar()
    }

    class ConexaoPool {
        - List<Conexao> conexoesDisponiveis
        - List<Conexao> conexoesEmUso
        + getConexao(): Conexao
        + liberarConexao(Conexao): void
    }

    class Cliente {
        + main(String[] args)
    }

    ConexaoPool "1" -- "*" Conexao : gerencia
    Cliente ..> ConexaoPool : usa
```

### Código Java do Object Pool:

#### Classe Conexao (Objeto a ser reutilizado)

```java
public class Conexao {
    private boolean emUso;

    public Conexao() {
        this.emUso = false;
    }

    public void usar() {
        emUso = true;
        System.out.println("Conexão em uso...");
    }

    public void liberar() {
        emUso = false;
        System.out.println("Conexão liberada.");
    }

    public boolean estaEmUso() {
        return emUso;
    }
}
```

#### Classe ConexaoPool (Gerenciador do Pool)

```java
import java.util.ArrayList;
import java.util.List;

public class ConexaoPool {
    private List<Conexao> conexoesDisponiveis;
    private List<Conexao> conexoesEmUso;
    private int tamanhoMaximo;

    public ConexaoPool(int tamanhoMaximo) {
        this.tamanhoMaximo = tamanhoMaximo;
        conexoesDisponiveis = new ArrayList<>();
        conexoesEmUso = new ArrayList<>();

        // Criando um número inicial de conexões
        for (int i = 0; i < tamanhoMaximo; i++) {
            conexoesDisponiveis.add(new Conexao());
        }
    }

    public synchronized Conexao getConexao() {
        if (!conexoesDisponiveis.isEmpty()) {
            Conexao conexao = conexoesDisponiveis.remove(0);
            conexao.usar();
            conexoesEmUso.add(conexao);
            return conexao;
        } else {
            System.out.println("Nenhuma conexão disponível no momento.");
            return null;
        }
    }

    public synchronized void liberarConexao(Conexao conexao) {
        if (conexoesEmUso.remove(conexao)) {
            conexao.liberar();
            conexoesDisponiveis.add(conexao);
        }
    }
}
```

#### Classe Cliente (Usa o Pool)

```java	
public class Cliente {
    public static void main(String[] args) {
        ConexaoPool pool = new ConexaoPool(2); // Criando um pool com 2 conexões

        // Pegando conexões do pool
        Conexao conexao1 = pool.getConexao();
        Conexao conexao2 = pool.getConexao();
        Conexao conexao3 = pool.getConexao(); // Esse deve falhar, pois o pool está cheio

        // Liberando uma conexão e reutilizando
        if (conexao1 != null) {
            pool.liberarConexao(conexao1);
        }

        Conexao conexao4 = pool.getConexao(); // Agora essa conexão pode ser reutilizada
    }
}
```

#### Explicação do Código
	1.	Criamos a classe Conexao, que simula uma conexão com o banco de dados e pode ser usada ou liberada.
	2.	Criamos a classe ConexaoPool, que gerencia uma lista de conexões disponíveis e em uso.
	3.	No método getConexao(), o pool fornece uma conexão já existente, evitando criar uma nova.
	4.	No método liberarConexao(), a conexão é devolvida ao pool para ser reutilizada.
	5.	Na classe Cliente, simulamos a obtenção e devolução de conexões, mostrando como o pool otimiza o uso de recursos.

#### Participantes


	1.	Reusable (Objeto reutilizável) → Em nosso código, a classe Conexao representa esse papel, pois é o objeto que queremos reaproveitar no pool.
	2.	Pool (Gerenciador do Pool) → A classe ConexaoPool atua como o gerenciador do pool, controlando quais objetos estão disponíveis e quais estão em uso.
	3.	Client (Cliente que usa o Pool) → A classe Cliente utiliza ConexaoPool para obter conexões e liberar quando não precisar mais.
