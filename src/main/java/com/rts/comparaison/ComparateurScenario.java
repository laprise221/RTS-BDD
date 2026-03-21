package com.rts.comparaison;

import com.rts.model.ComparaisonResult;
import com.rts.model.Scenario;
import com.rts.model.ScenarioSimilaire;
import com.rts.model.Step;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;
import java.util.*;

public class ComparateurScenario {

    private String extraireTexte(Scenario scenario) {
        StringBuilder sb = new StringBuilder(scenario.getName());
        for (Step s : scenario.getSteps()) {
            sb.append(" ").append(s.getText());
        }
        return sb.toString();
    }

    private List<String> preprocesser(String texte) {
        List<String> mots = new ArrayList<>();

        // EnglishAnalyzer automatise: tokenisation + stop words + stemming
        try (EnglishAnalyzer analyzer = new EnglishAnalyzer()) {
            TokenStream tokenStream = analyzer.tokenStream("field", texte);
            CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);//attribut un pointeur vers le mot courant dans le flux

            tokenStream.reset(); //initialisier le flux avant de le parcourir
            while (tokenStream.incrementToken()) {
                mots.add(attr.toString());
            }
            tokenStream.end();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mots;
    }


    //Calcul du TF ( fréquence du mot dans un scénario)
    private Map<String, Double> calculerTF(List<String> mots) {
        Map<String, Double> result= new HashMap<>();
        int nbMot=mots.size();
        for(String s: mots){
            result.put(s, result.getOrDefault(s,0.0)+1);
        }
        for(Map.Entry<String ,Double>entry: result.entrySet()){
            entry.setValue(entry.getValue()/nbMot);
        }
        return result;
    }

    //Calcul IDF (rareté du mot dans l'ensemble des scénario
    private Map<String, Double> calculerIDF(List<List<String>> tousLesMots) {
        Map<String,Double> result = new HashMap<>();
        int nbScenario= tousLesMots.size();
        for(List<String> scenario: tousLesMots){
            Set<String> uniques= new HashSet<>(scenario);
            for(String s: uniques){
                result.put(s, result.getOrDefault(s,0.0)+1);
            }
        }
        for(Map.Entry<String,Double>entry: result.entrySet()){
            entry.setValue(Math.log((double) nbScenario/entry.getValue()));
        }
        return result;
    }


    private Map<String,Double> calculerVecteurTFIDF(Map<String, Double> tf, Map<String, Double> idf){
        Map<String,Double> result = new HashMap<>();
        for(Map.Entry<String , Double>entry: tf.entrySet()){
            String mot = entry.getKey();
            double tfVal = entry.getValue();
            double idfVal = idf.getOrDefault(mot, 0.0);  // 0.0 si le mot n'existe pas dans l'IDF
            result.put(mot, tfVal * idfVal);
        }
        return result;
    }

    private double calculerSimilariteCosinus(Map<String, Double> vecteur1, Map<String, Double> vecteur2){
        double produitScalaire = 0.0;
        double v1Norm = 0.0;
        double v2Norm = 0.0;
        for(Map.Entry<String , Double>entry: vecteur1.entrySet()){
            String mot = entry.getKey();
            Double v1Val= entry.getValue();
            v1Norm+=Math.pow(v1Val,2);
            if(vecteur2.containsKey(mot)){
                Double v2Val= vecteur2.get(mot);
                produitScalaire+=v1Val*v2Val;
            }
        }
        for(Map.Entry<String, Double>entry:vecteur2.entrySet()){
            Double v2Val=entry.getValue();
            v2Norm+=Math.pow(v2Val,2);
        }

        v1Norm=Math.sqrt(v1Norm);
        v2Norm=Math.sqrt(v2Norm);

        if(v1Norm == 0.0 || v2Norm == 0.0){
            return 0.0;
        }
        return produitScalaire / (v1Norm * v2Norm);
    }

    public ComparaisonResult comparer(Scenario snew, List<Scenario> existing) {
        List<String> preproSnew = preprocesser(extraireTexte(snew));

        List<List<String>> preproExisting = new ArrayList<>();
        for (Scenario scenario : existing) {
            preproExisting.add(preprocesser(extraireTexte(scenario)));
        }

        List<List<String>> tousLesMots = new ArrayList<>();
        tousLesMots.add(preproSnew);
        tousLesMots.addAll(preproExisting);

        Map<String, Double> idf = calculerIDF(tousLesMots);
        Map<String, Double> tf = calculerTF(preproSnew);

        Map<String, Double> vecteurSnew = calculerVecteurTFIDF(tf, idf);

        List<ScenarioSimilaire> ranking = new ArrayList<>();
        for (int i = 0; i < existing.size(); i++) {
            Map<String, Double> vecteurExisting = calculerVecteurTFIDF(calculerTF(preproExisting.get(i)), idf);
            double score = calculerSimilariteCosinus(vecteurSnew, vecteurExisting);
            ranking.add(new ScenarioSimilaire(existing.get(i), score));
        }

        ranking.sort((a, b) -> Double.compare(b.getScoreSimilarite(), a.getScoreSimilarite()));

        return new ComparaisonResult(ranking);
    }

}
