import { IRegistroSintoma } from '../models/RegistroSintomaModel';
import { SintomaTipo } from '../constants/Enums';

export class SintomasAlertasController {

  /**
   * Interpreta o sintoma e retorna alertas baseados em regras de saúde validadas.
   */
  static analisarSintoma(sintoma: IRegistroSintoma): string[] {
    const alertas: string[] = [];

    // Regra: Sangramento Intenso com Alerta (Sinais de Alerta)
    if (sintoma.tipo === SintomaTipo.SANGRAMENTO && sintoma.intensidade === 5) {
      alertas.push("Sangramento muito intenso detectado. Se acompanhado de febre, tontura ou dor forte, procure imediatamente a UBS.");
    }

    // Regra: Sintomas Urinários persistentes ou intensos
    if (sintoma.tipo === SintomaTipo.SINTOMA_URINARIO && sintoma.intensidade >= 4) {
      alertas.push("Sintomas urinários intensos. Pode indicar uma infecção. É recomendável avaliação na UBS.");
    }

    // Regra: Fogachos/Suor Noturno (Climatério/Menopausa)
    if ((sintoma.tipo === SintomaTipo.FOGACHOS || sintoma.tipo === SintomaTipo.SUOR_NOTURNO) && sintoma.intensidade >= 4) {
      alertas.push("Sintomas de calor intenso detectados. Converse com um profissional de saúde sobre opções de alívio e autocuidado.");
    }

    return alertas;
  }

  /**
   * Validação de Atraso Menstrual (> 15 dias) e regras de ciclo.
   */
  static verificarAtraso(diasAtraso: number): string | null {
    if (diasAtraso > 15) {
      return "Atraso menstrual superior a 15 dias. Sugerimos realizar um teste de gravidez se houver possibilidade ou procurar a UBS para avaliação.";
    }
    return null;
  }

  /**
   * Validação de duração do ciclo (Regra: 15 a 60 dias).
   */
  static validarDuracaoCiclo(duracao: number): boolean {
    return duracao >= 15 && duracao <= 60;
  }
}
