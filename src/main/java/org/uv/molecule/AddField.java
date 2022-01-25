package org.uv.molecule;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor;

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
                    JSONObject row = (JSONObject) substancesIterator.next();
                    if (row.getJSONObject("IDE").getJSONObject("IDE.MF").optString("content").contains("\"C\"")) {
                        JSONObject calc = row.getJSONObject("CALC");
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
                        String molFile = "\n" + substance.getString("content");
                        MDLV3000Reader mdl = new MDLV3000Reader( new ByteArrayInputStream(molFile.getBytes()));
                        try {
                            IAtomContainer container = mdl.read(new org.openscience.cdk.AtomContainer(0, 0, 0, 0));
                            HBondAcceptorCountDescriptor calculator = new HBondAcceptorCountDescriptor();
                            org.openscience.cdk.qsar.DescriptorValue value = calculator.calculate(container);
                            String output = xrn + " " + rn + " ";
                            output += row.getJSONObject("IDE").getInt("IDE.MW") + "\t";
                            output += calc.getFloat("CALC.LOGP") + "\t";
                            output += calc.getFloat("CALC.HDONOR") + "\t";
                            output += calc.getFloat("CALC.HACCOR") + "\t";
                            output += value.getValue() + "\t";
                            output += calc.getFloat("CALC.ROTBND") + "\t";
                            output += calc.getFloat("CALC.TPSA") + "\t";
                            System.out.println(output);
                        } catch (Exception e) {
                            //System.out.println("xx : " + e);

                        }
                    }
                }
            }
        }
    }
}