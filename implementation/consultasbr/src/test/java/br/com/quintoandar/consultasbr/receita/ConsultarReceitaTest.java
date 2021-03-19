package br.com.quintoandar.consultasbr.receita;

import br.com.quintoandar.consultasbr.pf.RespostaCaptcha;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


/**
 * TODOS OS CPFs usados nesses testes foram achados com a busca: <i>cpf nome</i> no google.
 * @author <a href="mailto:mpereira@quintoandar.com.br">moa</a>
 *
 */
@Ignore("Analyze if these tests should still exist")
public class ConsultarReceitaTest {
	private static ConsultarReceita consultarReceita;
	
	@Before
	public void setBefore(){
		if(consultarReceita == null){
			consultarReceita = new ConsultarReceita();
		}
	}

	private String solveCaptcha(String name, byte[] captchaImage) {
		JLabel jLabel = new JLabel(new ImageIcon(captchaImage));
		jLabel.setText("Testing for: " + name);
		return JOptionPane.showInputDialog(jLabel);
	}

	@Test
	public void testWrongCaptcha() throws ParseException {
		RespostaCaptcha captcha = consultarReceita.requestCaptcha();
		ResultadoConsutaCPF res = consultarReceita.consultarCPF(captcha, "XXXXXX", "18530249100", new SimpleDateFormat("dd/MM/yyyy").parse("28/10/1958"));
		assertNotNull(res);
		assertEquals(StatusCPF.CAPTCHA_INVALIDO, res.getStatus());
	}

	@Test
	public void testConsultaAdalbertoWithNewClient() throws ParseException {
		String nomeAdalberto = "ADALBERTO ALVES DIAS";
		RespostaCaptcha captcha = consultarReceita.requestCaptcha();
		
		ConsultarReceita consultarReceita = new ConsultarReceita();
		ResultadoConsutaCPF res = consultarReceita.consultarCPF(captcha, solveCaptcha(nomeAdalberto, captcha.getCaptchaImage()), "185.302.491-00",  new SimpleDateFormat("dd/MM/yyyy").parse("28/10/1958"));
		assertNotNull(res);
		assertNotNull(res.getStatus());
		assertNotEquals(StatusCPF.INVALIDO, res.getStatus());
		assertNotEquals(StatusCPF.CAPTCHA_INVALIDO, res.getStatus());
		assertEquals(nomeAdalberto, res.getNome().toUpperCase());
	}

}
