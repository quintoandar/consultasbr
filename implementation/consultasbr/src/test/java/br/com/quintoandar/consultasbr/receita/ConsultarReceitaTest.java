package br.com.quintoandar.consultasbr.receita;

import static org.junit.Assert.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.junit.Before;
import org.junit.Test;

import br.com.quintoandar.consultasbr.pf.ConsultarPF;
import br.com.quintoandar.consultasbr.pf.RespostaCaptcha;

/**
 * TODOS OS CPFs usados nesses testes foram achados com a busca: <i>cpf nome</i> no google.
 * @author <a href="mailto:mpereira@quintoandar.com.br">moa</a>
 *
 */
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
	public void testWrongCaptcha() {
		RespostaCaptcha captcha = consultarReceita.requestCaptcha();
		ResultadoConsutaCPF res = consultarReceita.consultarCPF(captcha, "XXXXXX", "18530249100");
		assertNotNull(res);
		assertEquals(StatusCPF.CAPTCHA_INVALIDO, res.getStatus());
	}

	@Test
	public void testConsultaAdalberto() {
		String nomeAdalberto = "ADALBERTO ALVES DIAS";
		RespostaCaptcha captcha = consultarReceita.requestCaptcha();
		ResultadoConsutaCPF res = consultarReceita.consultarCPF(captcha, solveCaptcha(nomeAdalberto, captcha.getCaptchaImage()), "18530249100");
		assertNotNull(res);
		assertNotEquals(StatusCPF.INVALIDO, res.getStatus());
		assertNotEquals(StatusCPF.CAPTCHA_INVALIDO, res.getStatus());
		assertEquals(nomeAdalberto, res.getNome().toUpperCase());
	}

}
