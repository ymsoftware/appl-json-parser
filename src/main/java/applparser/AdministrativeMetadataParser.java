package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class AdministrativeMetadataParser extends ApplParser {
    //signals
    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Creator":
            case "Contributor":
            case "WorkflowStatus":
            case "Workgroup":
                parse(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ContentElement":
                parse("editorialrole", xmlr.getElementText(), map);
                break;
            case "Provider":
                //provider,function,provider
                break;
            case "Source":
                //sources,function,sources
                break;
            case "SourceMaterial":
                //sourcematerials,function,sourcematerials
                break;
            case "TransmissionSource":
                //transmissionsources,function,array
                break;
            case "ProductSource":
                //productsources,function,array
                break;
            case "ItemContentType":
                //itemcontenttype,function,itemcontenttype
                break;
            case "DistributionChannel":
                //distributionchannels,function,array
                break;
            case "Fixture":
                //fixture,function,fixture
                break;
            case "InPackage":
                //inpackages,function,inpackages
                break;
            case "Rating":
                //ratings,function,ratings
                break;
        }
    }
}
