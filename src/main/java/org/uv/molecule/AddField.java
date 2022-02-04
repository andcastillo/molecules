package org.uv.molecule;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class AddField {



    public static void main(String[] args) throws IOException {
        JSONArray results = null;
        try {
            //Path fileName = Path.of("/Users/acastillo/Documents/969616.xml");
            String xml = null;
            try {
                xml = new String(Files.readAllBytes(Paths.get(args[0])));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }

            JSONObject json = XML.toJSONObject(xml);
            results = json.getJSONArray("xf");//.getJSONObject("substances").getJSONArray("substance");

        } catch (Exception e) {
            //System.out.println(e.toString());
        }
        Iterator iterator = results.iterator();

        while(iterator.hasNext()) {
            JSONObject entry = (JSONObject) iterator.next();
            if (entry.has("substances")) {
                JSONArray substances = entry.getJSONObject("substances").getJSONArray("substance");

                Iterator substancesIterator = substances.iterator();

                while(substancesIterator.hasNext()) {
                    try {
                        JSONObject row = (JSONObject) substancesIterator.next();
                        if (row.has("IDE") && row.getJSONObject("IDE").has("IDE.MF") &&
                                row.getJSONObject("IDE").getJSONObject("IDE.MF").optString("content").contains("\"C\"")) {
                            JSONObject calc = row.optJSONObject("CALC");
                            if (calc != null) {
                                    int xrn = row.getJSONObject("IDE").getJSONObject("IDE.XRN").getInt("content");
                                    JSONObject substance = null;
                                    if (row.get("YY").getClass() == JSONArray.class)  {
                                        substance = row.getJSONArray("YY").getJSONObject(0).getJSONObject("YY.STR");
                                    } else {
                                        substance = row.getJSONObject("YY").getJSONObject("YY.STR");
                                    }
                                    String rn = "ND";
                                    if (row.getJSONObject("IDE").has("IDE.RN"))
                                        rn = row.getJSONObject("IDE").optString("IDE.RN", "ND");
                                    String molFile = substance.getString("content");

                                    DefaultChemObjectReader mdl;
                                    if (molFile.contains("V2000")) {
                                        mdl = new MDLV2000Reader(new ByteArrayInputStream(molFile.getBytes()));
                                    }
                                    else {
                                        mdl = new MDLV3000Reader( new ByteArrayInputStream(( "\n" + molFile).getBytes()));
                                    }

                                    IAtomContainer container = mdl.read(new org.openscience.cdk.AtomContainer(0, 0, 0, 0));
                                    HBondAcceptorCountDescriptor calculator = new HBondAcceptorCountDescriptor();
                                    org.openscience.cdk.qsar.DescriptorValue value = calculator.calculate(container);
                                    String output = xrn + "\t\"" + rn + "\"\t";
                                    output += row.getJSONObject("IDE").getInt("IDE.MW") + "\t";
                                    output += calc.optFloat("CALC.LOGP", -999) + "\t";
                                    output += calc.optFloat("CALC.HDONOR", -999) + "\t";
                                    output += calc.optFloat("CALC.HACCOR", -999) + "\t";
                                    output += value.getValue() + "\t";
                                    output += calc.optFloat("CALC.ROTBND", -999) + "\t";
                                    output += calc.optFloat("CALC.TPSA", -999) + "\t";
                                    System.out.println(output);
                            }
                        }
                    } catch (Exception e) {
                        //System.out.println("xx : " + e);
                    }
                }
            }
        }
    }
}