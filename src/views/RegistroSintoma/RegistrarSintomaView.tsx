import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  Alert,
} from 'react-native';
import { Colors } from '../../constants/Colors';
import { SintomaTipo } from '../../constants/Enums';
import { SintomasAlertasController } from '../../controllers/SintomasAlertasController';
import { RegistroSintomaModel, IRegistroSintoma } from '../../models/RegistroSintomaModel';

const RegistrarSintomaView = () => {
  const [data, setData] = useState(new Date().toLocaleDateString('pt-BR'));
  const [sintomaSelecionado, setSintomaSelecionado] = useState<SintomaTipo | null>(null);
  const [intensidade, setIntensidade] = useState(3);
  const [notas, setNotas] = useState('');

  const sintomas = Object.values(SintomaTipo);

  const handleSalvar = async () => {
    if (!sintomaSelecionado) {
      Alert.alert("Erro", "Por favor, selecione um sintoma.");
      return;
    }

    const novoRegistro: Omit<IRegistroSintoma, 'id'> = {
      usuarioId: 'usuario-uuid-placeholder', // Em produção, viria do Auth
      data: new Date(),
      tipo: sintomaSelecionado,
      intensidade: intensidade,
      notas: notas,
    };

    try {
      // 1. Analisar Alertas
      const alertas = SintomasAlertasController.analisarSintoma(novoRegistro as any);

      // 2. Salvar no Firebase
      await RegistroSintomaModel.salvar(novoRegistro);

      if (alertas.length > 0) {
        Alert.alert("Atenção", alertas.join('\n\n'));
      } else {
        Alert.alert("Sucesso", "Registro salvo com sucesso!");
      }

      // Limpar campos
      setSintomaSelecionado(null);
      setIntensidade(3);
      setNotas('');
    } catch (error) {
      Alert.alert("Erro", "Não foi possível salvar o registro.");
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Registrar Sintoma</Text>

      {/* Campo Data */}
      <View style={styles.section}>
        <Text style={styles.label}>Data do Registro</Text>
        <TextInput
          style={styles.input}
          value={data}
          editable={false}
        />
      </View>

      {/* Grade de Sintomas */}
      <View style={styles.section}>
        <Text style={styles.label}>Como você está se sentindo?</Text>
        <View style={styles.grid}>
          {sintomas.map((sintoma) => (
            <TouchableOpacity
              key={sintoma}
              style={[
                styles.symptomButton,
                sintomaSelecionado === sintoma && styles.symptomButtonSelected,
              ]}
              onPress={() => setSintomaSelecionado(sintoma)}
            >
              <Text style={[
                styles.symptomText,
                sintomaSelecionado === sintoma && styles.symptomTextSelected
              ]}>
                {sintoma}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Controle de Intensidade */}
      <View style={styles.section}>
        <Text style={styles.label}>Intensidade: {intensidade}</Text>
        <View style={styles.intensityContainer}>
          {[1, 2, 3, 4, 5].map((num) => (
            <TouchableOpacity
              key={num}
              style={[
                styles.intensityCircle,
                intensidade === num && styles.intensityCircleSelected,
              ]}
              onPress={() => setIntensidade(num)}
            >
              <Text style={[
                styles.intensityText,
                intensidade === num && styles.intensityTextSelected
              ]}>{num}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Notas */}
      <View style={styles.section}>
        <Text style={styles.label}>Notas Opcionais</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          multiline
          numberOfLines={4}
          placeholder="Descreva como se sente..."
          value={notas}
          onChangeText={setNotas}
        />
      </View>

      {/* Alerta Médico Obrigatório */}
      <View style={styles.medicalDisclaimer}>
        <Text style={styles.disclaimerText}>
          Essas informações não substituem avaliação médica. Procure sempre a UBS.
        </Text>
      </View>

      {/* Botão Salvar */}
      <TouchableOpacity style={styles.saveButton} onPress={handleSalvar}>
        <Text style={styles.saveButtonText}>Salvar Registro</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontFamily: 'Gabriel Sans Condensed', // Requisito de Design
    color: Colors.primary,
    marginBottom: 20,
    textAlign: 'center',
  },
  section: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    color: Colors.secondary,
    marginBottom: 8,
    fontWeight: '600',
  },
  input: {
    backgroundColor: Colors.white,
    borderWidth: 1,
    borderColor: Colors.lightPink,
    borderRadius: 8,
    padding: 12,
    color: Colors.text,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  symptomButton: {
    width: '48%',
    backgroundColor: Colors.white,
    padding: 12,
    borderRadius: 10,
    marginBottom: 10,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.lightPink,
  },
  symptomButtonSelected: {
    backgroundColor: Colors.accent,
    borderColor: Colors.primary,
  },
  symptomText: {
    fontSize: 14,
    color: Colors.text,
  },
  symptomTextSelected: {
    color: Colors.white,
    fontWeight: 'bold',
  },
  intensityContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  intensityCircle: {
    width: 45,
    height: 45,
    borderRadius: 22.5,
    backgroundColor: Colors.white,
    borderWidth: 1,
    borderColor: Colors.lightPink,
    justifyContent: 'center',
    alignItems: 'center',
  },
  intensityCircleSelected: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  intensityText: {
    fontSize: 16,
    color: Colors.primary,
  },
  intensityTextSelected: {
    color: Colors.white,
  },
  textArea: {
    height: 100,
    textAlignVertical: 'top',
  },
  medicalDisclaimer: {
    backgroundColor: '#FFF4F4',
    padding: 15,
    borderRadius: 8,
    borderLeftWidth: 5,
    borderLeftColor: Colors.primary,
    marginBottom: 25,
  },
  disclaimerText: {
    fontSize: 14,
    color: Colors.primary,
    fontWeight: 'bold',
    fontStyle: 'italic',
  },
  saveButton: {
    backgroundColor: Colors.primary,
    padding: 16,
    borderRadius: 30,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 5,
  },
  saveButtonText: {
    color: Colors.white,
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default RegistrarSintomaView;
