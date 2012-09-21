package bardqueryapi

import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import chemaxon.formats.MolImporter
import bard.core.adapter.CompoundAdapter

class ChemAxonController {

    ChemAxonService chemAxonService
    IQueryService queryService

    def index() {
        session.putValue('smiles', params.smiles)
    }

    def generateStructureImage() {
        byte[] bytes = []
        Integer width = (params.width ?: 300) as Integer
        Integer height = (params.height ?: 300) as Integer

        if (params.smiles) {
            bytes = generateStructurePNG(params.smiles, width, height)
        }
        else if (params.cid && params.cid?.isLong()) {
            Long cid = new Long(params.cid)
            Map compoundsMap = this.queryService.findCompoundsByCIDs([cid])
            List<CompoundAdapter> compoundAdapters = compoundsMap.compoundAdapters
            if (compoundAdapters.size() == 1) {
                String smiles = compoundAdapters.first().structureSMILES
                bytes = chemAxonService.generateStructurePNG(smiles, width, height)
            }
        }

        response.contentType = 'image/png'
        response.outputStream.setBytes(bytes)
    }

    private byte[] generateStructurePNG(String smiles, int width, int height) {
        return chemAxonService.generateStructurePNG(smiles, width, height)
    }

    def marvinSketch() {}
}
