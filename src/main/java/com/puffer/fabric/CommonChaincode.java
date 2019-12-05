package com.puffer.fabric;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.puffer.fabric.config.Config;
import com.puffer.fabric.user.UserContext;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

/**
 * 链码公共操作
 *
 * @author buyi
 * @date 2019年12月03日 21:13:56
 * @since 1.0.0
 */
public class CommonChaincode {
    private static final String rootPath = CommonChaincode.class.getResource("/").getPath();

    /**
     * 构建组织上下文
     *
     * @param
     * @return java.util.List<com.puffer.fabric.user.UserContext>
     * @author buyi
     * @date 2019年12月03日 19:38:01
     * @since 1.0.8
     */
    public static List<UserContext> buildUserContext() throws Exception {
        List<UserContext> userContextList = Lists.newArrayList();
        //example 是初始化两个组织机构

        //step1.构建org1的用户上下文，以及私钥和证书（公钥）
        // Enrollment enrollOrg1Admin = Util.getEnrollment(
        //         rootPath + "cert", "user1-key",
        //         rootPath + "cert", "user1-cert.pem");
        Enrollment enrollOrg1Admin = loadFromPemFile(rootPath + "cert" + File.separator + "user1-key",
                rootPath + "cert" + File.separator + "user1-cert.pem");

        UserContext org1Admin = new UserContext();
        org1Admin.setEnrollment(enrollOrg1Admin);
        org1Admin.setMspId(Config.ORG1_MSP);
        org1Admin.setName(Config.ADMIN);

        userContextList.add(org1Admin);

        //step2. 构建org2的用户上下文，以及私钥和证书（公钥）
        // Enrollment enrollOrg2Admin = Util.getEnrollment(
        //         rootPath + "cert", "user2-key",
        //         rootPath + "cert", "user2-cert.pem");
        Enrollment enrollOrg2Admin = loadFromPemFile(rootPath + "cert" + File.separator + "user2-key",
                rootPath + "cert" + File.separator + "user2-cert.pem");

        UserContext org2Admin = new UserContext();
        org2Admin.setEnrollment(enrollOrg2Admin);
        org2Admin.setMspId(Config.ORG2_MSP);
        org2Admin.setName(Config.ADMIN);

        userContextList.add(org2Admin);

        return userContextList;
    }

    private static Enrollment loadFromPemFile(String keyFile, String certFile) throws Exception {
        byte[] keyPem = IOUtils.toByteArray(new FileInputStream(keyFile)); // 载入私钥PEM文本
        byte[] certPem = IOUtils.toByteArray(new FileInputStream(certFile)); // 载入证书PEM文本
        CryptoPrimitives suite = new CryptoPrimitives(); // 载入密码学套件
        PrivateKey privateKey = suite.bytesToPrivateKey(keyPem); // 将PEM文本转换为私钥对象
        return new X509Enrollment(privateKey, new String(certPem)); // 创建并返回X509Enrollment对象
    }


    /**
     * 构建节点
     *
     * @param
     * @return
     * @author buyi
     * @date 2019年12月03日 20:07:00
     * @since 1.0.8
     */
    public static Map<String, List<Peer>> buildPeer(HFClient hfClient) throws InvalidArgumentException {
        Map<String, List<Peer>> orgPeerMap = Maps.newConcurrentMap();

        //step1. 设置org1的节点
        Peer peer0_org1 = hfClient.newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
        Peer peer1_org1 = hfClient.newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
        orgPeerMap.put(Config.ORG1_MSP, Lists.newArrayList(peer0_org1, peer1_org1));

        //step2. 设置org2的节点
        Peer peer0_org2 = hfClient.newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
        Peer peer1_org2 = hfClient.newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);

        orgPeerMap.put(Config.ORG2_MSP, Lists.newArrayList(peer0_org1, peer1_org1));

        return orgPeerMap;
    }

    /**
     * 构建共识节点
     *
     * @param
     * @return org.hyperledger.fabric.sdk.Orderer
     * @author buyi
     * @date 2019年12月03日 20:05:36
     * @since 1.0.8
     */
    public static Orderer buildOrderer(HFClient hfClient) throws InvalidArgumentException {
        return hfClient.newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
    }
}
