import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
} from 'react-native';
import { Colors } from '../../constants/Colors';
import { FaseVida } from '../../constants/Enums';

const { width } = Dimensions.get('window');

const HomeView = () => {
  // Dados fictícios (Em produção viriam do PerfilUsuarioModel e CicloController)
  const userProfile = {
    nome: 'Ana',
    faseVida: FaseVida.IDADE_REPRODUTIVA,
  };

  const cicloInfo = {
    proximaMenstruacao: '12 dias',
    status: 'Fase Folicular',
  };

  const dicaDoDia = "Mantenha-se hidratada! Beber água ajuda a reduzir o inchaço e as cólicas durante o período menstrual.";

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Header Acolhedor */}
      <View style={styles.header}>
        <Text style={styles.welcomeText}>Olá, {userProfile.nome}!</Text>
        <Text style={styles.subtitleText}>Como está o seu autocuidado hoje?</Text>
      </View>

      <View style={styles.content}>

        {/* Card Dinâmico conforme Perfil */}
        <View style={[styles.card, styles.dynamicCard]}>
          <Text style={styles.cardTitle}>Você está na fase: {userProfile.faseVida}</Text>
          <Text style={styles.cardContent}>
            {userProfile.faseVida === FaseVida.IDADE_REPRODUTIVA
              ? "Acompanhe seu ciclo e conheça melhor seu corpo a cada fase."
              : "Veja dicas personalizadas para sua jornada atual."}
          </Text>
          <TouchableOpacity style={styles.actionButton}>
            <Text style={styles.actionButtonText}>Ver mais dicas</Text>
          </TouchableOpacity>
        </View>

        {/* Resumo do Ciclo */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Resumo do Ciclo</Text>
          <View style={styles.cycleInfoContainer}>
            <View style={styles.cycleCircle}>
              <Text style={styles.cycleDays}>{cicloInfo.proximaMenstruacao}</Text>
              <Text style={styles.cycleLabel}>para iniciar</Text>
            </View>
            <View style={styles.cycleDetails}>
              <Text style={styles.statusText}>{cicloInfo.status}</Text>
              <Text style={styles.detailText}>Ciclo regular (28 dias)</Text>
            </View>
          </View>
          <TouchableOpacity style={styles.secondaryButton}>
            <Text style={styles.secondaryButtonText}>Ver Calendário</Text>
          </TouchableOpacity>
        </View>

        {/* Dica de Saúde do Dia */}
        <View style={[styles.card, { backgroundColor: Colors.lightPink }]}>
          <View style={styles.tipHeader}>
            <Text style={styles.cardTitle}>Dica de Saúde</Text>
            <Text style={{ fontSize: 20 }}>💡</Text>
          </View>
          <Text style={styles.tipText}>{dicaDoDia}</Text>
        </View>

        {/* Atalhos Rápidos */}
        <View style={styles.quickActions}>
          <TouchableOpacity style={styles.quickActionButton}>
            <Text style={styles.quickActionEmoji}>📝</Text>
            <Text style={styles.quickActionText}>Registrar</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.quickActionButton}>
            <Text style={styles.quickActionEmoji}>📚</Text>
            <Text style={styles.quickActionText}>Aprender</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.quickActionButton}>
            <Text style={styles.quickActionEmoji}>🆘</Text>
            <Text style={styles.quickActionText}>Apoio</Text>
          </TouchableOpacity>
        </View>

      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    padding: 30,
    paddingTop: 60,
    backgroundColor: Colors.white,
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
  },
  welcomeText: {
    fontSize: 28,
    fontFamily: 'Leckerli One', // Fonte de destaque do design
    color: Colors.primary,
  },
  subtitleText: {
    fontSize: 16,
    color: Colors.secondary,
    marginTop: 5,
  },
  content: {
    padding: 20,
  },
  card: {
    backgroundColor: Colors.white,
    borderRadius: 20,
    padding: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
    elevation: 3,
  },
  dynamicCard: {
    borderLeftWidth: 8,
    borderLeftColor: Colors.accent,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.primary,
    marginBottom: 10,
  },
  cardContent: {
    fontSize: 14,
    color: Colors.text,
    lineHeight: 20,
  },
  actionButton: {
    marginTop: 15,
    backgroundColor: Colors.accent,
    paddingVertical: 8,
    paddingHorizontal: 15,
    borderRadius: 15,
    alignSelf: 'flex-start',
  },
  actionButtonText: {
    color: Colors.white,
    fontWeight: 'bold',
    fontSize: 12,
  },
  cycleInfoContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 15,
  },
  cycleCircle: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 4,
    borderColor: Colors.lightPink,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 20,
  },
  cycleDays: {
    fontSize: 20,
    fontWeight: 'bold',
    color: Colors.primary,
  },
  cycleLabel: {
    fontSize: 10,
    color: Colors.secondary,
  },
  cycleDetails: {
    flex: 1,
  },
  statusText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: Colors.secondary,
  },
  detailText: {
    fontSize: 12,
    color: Colors.text,
    marginTop: 4,
  },
  secondaryButton: {
    borderWidth: 1,
    borderColor: Colors.secondary,
    paddingVertical: 8,
    borderRadius: 15,
    alignItems: 'center',
  },
  secondaryButtonText: {
    color: Colors.secondary,
    fontSize: 14,
    fontWeight: '600',
  },
  tipHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  tipText: {
    fontSize: 14,
    color: Colors.primary,
    fontStyle: 'italic',
    lineHeight: 20,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 10,
  },
  quickActionButton: {
    backgroundColor: Colors.white,
    width: (width - 60) / 3,
    padding: 15,
    borderRadius: 15,
    alignItems: 'center',
    elevation: 2,
  },
  quickActionEmoji: {
    fontSize: 24,
    marginBottom: 5,
  },
  quickActionText: {
    fontSize: 12,
    color: Colors.secondary,
    fontWeight: '600',
  },
});

export default HomeView;
