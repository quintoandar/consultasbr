consultasbr
===========

A series of libs that allow different queries about a person in Brazil: CPF (~Security Social ID), Antecedentes Criminais PF (~criminal record with federal police), Processos no TJSP (~public records of court processes/cases of São Paulo State).

* New: IGPMReader to read IGP-M index. 
* New: IPTU Campinas

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
    <version>1.4.2-SNAPSHOT</version>
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
		jLabel.setText("Solve captcha");
		String captchaSolution = JOptionPane.showInputDialog(jLabel);
		
		ResultadoConsutaCPF res = consultar.consultarCPF(captcha,captchaSolution,"12345678909", new SimpleDateFormat("dd/MM/yyyy").parse("01/01/1970"));
    
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
### IPTU São Paulo - SP

```java
public class IGPMTeste {
  public static void main(String[] args) {
    ConsultarIptuSP crawler = new ConsultarIptuSP();
    String taxpayerCode = ""; //SP city code, like "000.000.0000-0"
    int parcelNumber = 5;
    int year = 2014;
    try {
      Resposta2ViaIPTU resp = crawler.buscar2aViaIPTU(taxpayerCode, parcelNumber, year);
      System.out.println(String.format("Boleto: %s",resp.getCodigo()));
      System.out.println(String.format("Valor: %f",resp.getValor()));
    } catch (Throwable t){
      System.out.println("Error testes. " + t.getMessage() + t.getStackTrace());
    }
  }
}
```
 You can also check the tests to see how to use all the different types of consultas.
 
### IPTU - Prefeitura Municipal de Campinas - SP

```java
public class CPFTeste {
  public static void main(String[] args) {
    ConsultarIptuCampinas consultar = new ConsultarIptuCampinas();
    RespostaCaptcha captcha = consultar.requestCaptcha();
    
    //Shows a dialog with the captcha image to be solved
    JLabel jLabel = new JLabel(new ImageIcon(captcha.getCaptchaImage()));
		jLabel.setText("Solve captcha");
		String captchaSolution = JOptionPane.showInputDialog(jLabel);
		
		String codigoCartografico = "0000.00.00.0000.00000"; // it accepts sequence without any format. ex.: 00000000000000000 (17 characters in sequence)
		List<Resposta2ViaIPTU> parcelas res = consultar.buscar2aViaIPTU(captcha.getSessionId(), respCaptcha, codigoCartografico);
    
		if(res != null) {
			for(Resposta2ViaIPTU p : parcelas){
				 System.out.print("Bar Code: " + p.getCodigo());
				 System.out.print("Código Cartográfico: " + p.getCodigoContribuinte());
				 System.out.print("Year: " + p.getAnoExercicio());
				 System.out.print("Payment series (Installment Payment): " + p.getNumParcela());
				 System.out.print("Total (R$): " + p.getValor());
				 System.out.print("Payment Due Date: " + p.getVencimento());
			}
	  }
	}
}
