package similarity.algorithms.specifications;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import main.StaticValues;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import similarity.measures.entities.IEntitySimilarityMeasure;
import similarity.measures.entities.PrimitiveEntitySimilarityMeasure;

public class WeightedInputSpecification extends BasicInputSpecification {
	
	private static final Logger LOG = Logger.getLogger(StaticValues.LOGGER_NAME);
	
	private double m_discountingFactor;
	
	private Map<OWLEntity, Double> m_weightingFunction;
	
	private TerminationMethod m_termination;
	
	private double m_terminationValue;
	
	public WeightedInputSpecification() {
		super();
		m_weightingFunction = new HashMap<OWLEntity, Double>();
	}
	
	@Override
	public void read(File f) {
		// TODO Auto-generated method stub
		super.read(f);
		// read some more
	}
	
	@Override
	public boolean isValid() {
		if(m_primitiveMeasure == null){
			LOG.severe("Primitive measure not specified.");
			return false;
		}
		
		if(m_discountingFactor < 0 || m_discountingFactor > 1){
			LOG.severe("Discounting factor must be in [0,1]");
			
			return false;
		}
		
		if(m_defaultWeight < 0){
			LOG.severe("Default entity weight must be greater or equal to 0");
			return false;
		}
		
		return super.isValid();
	}
	
	public void setDiscountingFactor(double discount){
		this.m_discountingFactor = discount;
	}
	
	public void setPrimitiveMeasure(IEntitySimilarityMeasure measure){
		this.m_primitiveMeasure = measure;
	}
	
	public void setPrimitiveSimilarity(String entity1, String entity2, double sim){
		if(m_primitiveMeasure instanceof PrimitiveEntitySimilarityMeasure){
			if(getOntology() == null){
				LOG.warning("Ignoring primitive measure specifications, should occur after ontology specification to enable verification of entities.");
				return;
			}
			OWLDataFactory df = getOntology().getOWLOntologyManager().getOWLDataFactory();
			if(getOntology().getClassesInSignature().contains(df.getOWLClass(IRI.create(entity1)))){
				if(getOntology().getClassesInSignature().contains(df.getOWLClass(IRI.create(entity2)))){
					// both strings represent existing classes
					((PrimitiveEntitySimilarityMeasure)m_primitiveMeasure).registerSimilarity(
							df.getOWLClass(IRI.create(entity1)),
							df.getOWLClass(IRI.create(entity2)),
							sim);
				}
			}
			if(getOntology().getObjectPropertiesInSignature().contains(df.getOWLObjectProperty(IRI.create(entity1)))){
				if(getOntology().getObjectPropertiesInSignature().contains(df.getOWLObjectProperty(IRI.create(entity2)))){
					// both strings represent existing properties
					((PrimitiveEntitySimilarityMeasure)m_primitiveMeasure).registerSimilarity(
							df.getOWLObjectProperty(IRI.create(entity1)),
							df.getOWLObjectProperty(IRI.create(entity2)),
							sim);
				}
			}
		}else{
			LOG.warning("Ignoring explicit primitive similarities since the DEFAULT similarity measure is selected.");
		}
	}
	
	public void setWeight(OWLEntity e, double weight){
		if((e instanceof OWLClass || e instanceof OWLObjectProperty)){ // only accept weights for those
//				&& weight <=1 && weight >= 0){ // only accept weight from [0,1]
			m_weightingFunction.put(e, weight);
		}
	}
	
	public void setWeight(String strRep, double weight){
		if(getOntology() == null){
			LOG.warning("Ignoring weight input, should occur after ontology specification to enable verification of entities.");
			return;
		}
		OWLDataFactory df = getOntology().getOWLOntologyManager().getOWLDataFactory();
		if(getOntology().getClassesInSignature().contains(df.getOWLClass(IRI.create(strRep)))){
			setWeight(df.getOWLClass(IRI.create(strRep)), weight);
		}
		if(getOntology().getObjectPropertiesInSignature().contains(df.getOWLObjectProperty(IRI.create(strRep)))){
			setWeight(df.getOWLObjectProperty(IRI.create(strRep)), weight);
		}
	}
	
	public void setDefaultWeight(double weight){
		this.m_defaultWeight = weight;
	}
	
	/**
	 * The value is used for both RELATIVE and ABSOLUTE termination criteria.
	 * Naturally for RELATIVE termination, only values from [0,1) are accepted.
	 * For ABSOLUTE termination the value describes a fixed amount of iterations,
	 * hence the decimal places will be floored. To ensure successfully setting the value,
	 * you are required to set both method and value at the same time.
	 * @param value
	 */
	public void setTerminationMethod(TerminationMethod method, double value){
		m_termination = method;
		
		if(m_termination == null){
			m_termination = super.getTerminationMethod(); // default
			m_terminationValue = super.getTerminationValue();
		}
		switch(m_termination){
		case RELATIVE : 
			if(value < 1 && value >= 0){
				m_terminationValue = value;
			}else{
				LOG.warning("For relative termination, the value must be in [0,1). " +
						"Given: " + value + ", kept: " + m_terminationValue);
			}
			break;
		case ABSOLUTE :
			m_terminationValue = Math.floor(value);
			if(m_terminationValue == 0) LOG.warning("With this termination specification computation will stop after 0 iterations.");
			break;
		default : break; // maybe null case would end up here?
		}
	}
	
	@Override
	public Double getWeight(OWLEntity e) {
		if(m_weightingFunction.containsKey(e))
			return m_weightingFunction.get(e);
		return super.getWeight(e); // default weight
	}
	
	@Override
	public Double getDiscountingFactor() {
		return m_discountingFactor;
	}
	
	@Override
	public TerminationMethod getTerminationMethod() {
		if(m_termination != null) return m_termination;
		return super.getTerminationMethod();
	}
	
	@Override
	public double getTerminationValue() {
		if(m_termination != null) return m_terminationValue;
		return super.getTerminationValue();
	}
	
}