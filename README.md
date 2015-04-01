consultasbr
===========

A series of libs that allow different queries about a person in Brazil: CPF (~Security Social ID), Antecedentes Criminais PF (~criminal record with federal police), Processos no TJSP (~public records of court processes/cases of São Paulo State).

* New: IGPMReader to read IGP-M index. 

Mvn Usage
-----
First add the repository:

```xml
<repositories>
  ...
  <repository>
    <id>QuintoAndar Repo ConsultasBr</id>
    <url>https://raw.github.com/quintoandar/consultasbr/master/mvn-repo</url>
  </repository>
  ...
</repositories>
  
```

Then add the dependency artifact:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>br.com.quintoandar</groupId>
    <artifactId>consultasbr</artifactId>
    <version>1.1.0-SNAPSHOT</version>
  </dependency>
  ...
</dependencies>
```

consultsabr Usage
-----

### Consultar CPF (Pessoa Física) na Receita

```java
public class CPFTeste {
  public static void main(String[] args) {
    ConsultarReceita consultar = new ConsultarReceita();
    RespostaCaptcha captcha = consultar.requestCaptcha();
    
    //Shows a dialog with the captcha image to be solved
    JLabel jLabel = new JLabel(new ImageIcon(captcha.getCaptchaImage()));
		jLabel.setText("Testing for: " + name);
		String captchaSolution = JOptionPane.showInputDialog(jLabel);
		
		ResultadoConsutaCPF res = consultar.consultarCPF(captcha,captchaSolution,"12345678909");
    
		if(res != null) {
      System.out.print("Status: " + res.getStatus());
      System.out.print("Name on Receita: " + res.getNome());
	  }
	}
}
```

### IGPM

```java
public class IGPMTeste {
  public static void main(String[] args) {
    IGPMReader reader = new IGPMReader();
		reader.processar();
		if(reader.getMes() != null) {
      System.out.print(reader.getMes());
			System.out.print(reader.getMensal());
			System.out.print(reader.getAcumulado());
	  }
	}
}
```
 You can also check the tests to see how to use all the different types of consultas.
