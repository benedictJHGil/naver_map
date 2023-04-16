package org.example;

import kr.inflearn.AddressVO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class NaverMap implements ActionListener {
    Main naverMap;
    public NaverMap(Main naverMap) {
        this.naverMap = naverMap;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String client_id = "z843wbk6ep";
        String client_secret = "u1shCqj5IbsASZXOxSkX38mIQhmd6E4pfCceNsdz";
        AddressVO vo = null;
        try {
            String address = naverMap.address.getText();
            String addr = URLEncoder.encode(address, "UTF-8");
            String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + addr;
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", client_id);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", client_secret);
            int resCode = conn.getResponseCode();
            BufferedReader br;
            if (resCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String inputLine;
            StringBuffer res = new StringBuffer(); // 문자열 추가 변경시 사용
            while ((inputLine = br.readLine()) != null) {
                res.append(inputLine);
            }
            br.close();

            JSONTokener tokener = new JSONTokener(res.toString());
            JSONObject object = new JSONObject(tokener);
            System.out.println(object);

            JSONArray arr = object.getJSONArray("addresses");
            for (int i = 0; i<arr.length(); i++) {
                JSONObject temp = (JSONObject) arr.get(i);
                vo = new AddressVO();
                vo.setRoadAddress((String) temp.get("roadAddress"));
                vo.setJibunAddress((String) temp.get("jibunAddress"));
                vo.setX((String) temp.get("x"));
                vo.setY((String) temp.get("y"));
                System.out.println(vo);
            }
            MapService(vo);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private void MapService(AddressVO vo) {
        String URL_STATICMAP = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?";
        try {
            String pos = URLEncoder.encode(vo.getX() + " " + vo.getY(), "UTF-8");
            String url = URL_STATICMAP;
            url += "center=" + vo.getX() + "," + vo.getY();
            url += "&level=16&w=700&h=500";
            url += "&markers=type:t|size:mid|pos:" + pos + "|label:" + URLEncoder.encode(vo.getRoadAddress(), "UTF-8");
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "z843wbk6ep");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", "u1shCqj5IbsASZXOxSkX38mIQhmd6E4pfCceNsdz");

            BufferedReader br;

            int resCode = conn.getResponseCode();
            if(resCode == 200) {
                InputStream is = conn.getInputStream();
                int read = 0;
                byte[] bytes = new byte[1024];
                String tempName = Long.valueOf(new Date().getTime()).toString();
                File f = new File(tempName + ".jpg");
                f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();
                ImageIcon img = new ImageIcon(f.getName());
                naverMap.imageLabel.setIcon(img);
                naverMap.resAddress.setText(vo.getRoadAddress());
                naverMap.jibunAddress.setText(vo.getJibunAddress());
                naverMap.resX.setText(vo.getX());
                naverMap.resY.setText(vo.getY());
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String inputLine;
                StringBuffer res = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    res.append(inputLine);
                }
                br.close();
            }
        } catch (Exception e) {
            System.out.println(e);;
        }
    }
}
