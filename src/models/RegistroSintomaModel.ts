import { SintomaTipo } from '../constants/Enums';
import firestore from '@react-native-firebase/firestore';

export interface IRegistroSintoma {
  id: string; // UUID
  usuarioId: string;
  data: Date;
  tipo: SintomaTipo;
  intensidade: number; // 1 a 5
  notas?: string;
}

export class RegistroSintomaModel {
  static collection = firestore().collection('registros_sintomas');

  /**
   * Salva um novo registro de sintoma no Firestore.
   * Utiliza UUID para o ID do documento se fornecido, caso contrário o Firestore gera um.
   */
  static async salvar(dados: Omit<IRegistroSintoma, 'id'> & { id?: string }): Promise<string> {
    const dataToSave = {
      ...dados,
      data: firestore.Timestamp.fromDate(dados.data),
    };

    if (dados.id) {
      await this.collection.doc(dados.id).set(dataToSave);
      return dados.id;
    } else {
      const docRef = await this.collection.add(dataToSave);
      return docRef.id;
    }
  }

  /**
   * Busca registros de um usuário específico.
   */
  static async buscarPorUsuario(usuarioId: string): Promise<IRegistroSintoma[]> {
    const snapshot = await this.collection
      .where('usuarioId', '==', usuarioId)
      .orderBy('data', 'desc')
      .get();

    return snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
      data: (doc.data().data as firestore.FirebaseFirestoreTypes.Timestamp).toDate(),
    } as IRegistroSintoma));
  }
}
