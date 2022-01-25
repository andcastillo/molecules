package org.uv.molecule;

import java.io.*;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor;
import org.json.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    JSONObject row = (JSONObject) substancesIterator.next();
                    JSONObject calc = row.getJSONObject("CALC");
                    int xrn = row.getJSONObject("IDE").getJSONObject("IDE.XRN").getInt("content");
                    JSONObject substance = null;
                    if (row.get("YY").getClass() == JSONArray.class)  {
                        substance = row.getJSONArray("YY").getJSONObject(0).getJSONObject("YY.STR");
                    } else {
                        substance = row.getJSONObject("YY").getJSONObject("YY.STR");
                    }
                    String rn = null;
                    if (row.getJSONObject("IDE").has("IDE.RN")) {
                        if (row.getJSONObject("IDE").get("IDE.RN").getClass() == JSONArray.class) {
                            rn = row.getJSONObject("IDE").getJSONArray("IDE.RN").getString(0);
                        } else {
                            rn = row.getJSONObject("IDE").optString("IDE.RN", xrn + "");
                        }
                    } else {
                        rn = xrn + "";
                    }
                    String molFile = "\n" + substance.getString("content");
                    MDLV3000Reader mdl = new MDLV3000Reader( new ByteArrayInputStream(molFile.getBytes()));
                    try {
                        IAtomContainer container = mdl.read(new org.openscience.cdk.AtomContainer(0, 0, 0, 0));
                        HBondDonorCountDescriptor calculator = new HBondDonorCountDescriptor();
                        org.openscience.cdk.qsar.DescriptorValue value = calculator.calculate(container);
                        String output = xrn + " " + rn + " ";
                        output += calc.getFloat("CALC.TPSA") + " ";
                        output += calc.getFloat("CALC.ROTBND") + " ";
                        //output += calc.getFloat("CALC.HDONOR") + " ";
                        output += calc.getFloat("CALC.VEBER") + " ";
                        output += calc.getFloat("CALC.LOGP") + " ";
                        output += calc.getFloat("CALC.HACCOR") + " ";
                        output += calc.getFloat("CALC.LIPINSKI") + " ";
                        output += value.getValue();

                        System.out.println(output);
                    } catch (Exception e) {
                        //System.out.println("xx : " + e);

                    }
                }
            }
        }
    }
}