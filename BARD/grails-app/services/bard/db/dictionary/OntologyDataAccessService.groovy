package bard.db.dictionary

import bard.db.dictionary.*
import bard.db.registration.*;

class OntologyDataAccessService {
	
	private static final String ASSAY_DESCRIPTOR = "assay protocol"
	
	private static final String BIOLOGY_DESCRIPTOR = "biology"
	
	private static final String INSTANCE_DESCRIPTOR = "project management"

	
	public List<Descriptor> getAttributeDescriptors(String path, String label){
		def results
		if(path && path.startsWith(ASSAY_DESCRIPTOR)){
			results = AssayDescriptor.findAllByFullPathLikeAndLabelIlike(path + "%", "%" + label + "%")
		}
		else 
		if(path && path.startsWith(BIOLOGY_DESCRIPTOR)){
			results = BiologyDescriptor.findAllByFullPathLikeAndLabelIlike(path + "%", "%" + label + "%")
		}
		else
		if(path && path.startsWith(INSTANCE_DESCRIPTOR)){
			results = InstanceDescriptor.findAllByFullPathLikeAndLabelIlike(path + "%", "%" + label + "%")
		}

		return results
	}
	
	public List<Descriptor> getValueDescriptors(Long elementId, String path, String term){ 
		def results
		if(path && path.startsWith(ASSAY_DESCRIPTOR)){
			def query = AssayDescriptor.where{(element.id == elementId)}
			results = query.list()
			if(results){
				List<Descriptor> allDescriptors = new ArrayList<Descriptor>()
				for(ad in results){
					query = AssayDescriptor.where{(parent {id == ad.id}) && (fullPath ==~ path + "%") && (leaf == true) && (label ==~ term + "%") }
					def descriptors = query.list()
					allDescriptors.addAll(descriptors)
				}
				results = allDescriptors
			}
		}
		else
		if(path && path.startsWith(BIOLOGY_DESCRIPTOR)){
			def query = BiologyDescriptor.where{(element.id == elementId)}
			results = query.list()
			if(results){
				List<Descriptor> allDescriptors = new ArrayList<Descriptor>()
				for(ad in results){
					query = BiologyDescriptor.where{(parent {id == ad.id}) && (fullPath ==~ path + "%") && (leaf == true) && (label ==~ term + "%") }
					def descriptors = query.list()
					allDescriptors.addAll(descriptors)
				}
				results = allDescriptors
			}
		}
		else
		if(path && path.startsWith(INSTANCE_DESCRIPTOR)){
			def query = InstanceDescriptor.where{(element.id == elementId)}
			results = query.list()
			if(results){
				List<Descriptor> allDescriptors = new ArrayList<Descriptor>()
				for(ad in results){
					query = InstanceDescriptor.where{(parent {id == ad.id}) && (fullPath ==~ path + "%") && (leaf == true) && (label ==~ term + "%") }
					def descriptors = query.list()
					allDescriptors.addAll(descriptors)
				}
				results = allDescriptors
			}
		}
		println "Results - Descriptor list size:" + results?.size()
		return results
	}
}
