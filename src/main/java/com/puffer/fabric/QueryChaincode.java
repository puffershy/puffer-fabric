package com.puffer.fabric;

import com.google.common.collect.Lists;
import com.puffer.fabric.config.Config;
import com.puffer.fabric.user.UserContext;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 查询链码
 *
 * @author buyi
 * @date 2019年12月03日 20:59:56
 * @since 1.0.0
 */
public class QueryChaincode {
    public static void main(String[] args) throws Exception {

        //step1. 创建HFClient
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        //step2. 设置客户端操作用户
        List<UserContext> userContextList = CommonChaincode.buildUserContext();
        hfClient.setUserContext(userContextList.get(0));

        //step3. 创建通道
        Channel channel = hfClient.newChannel("mychannel");

        //step4. 设置通道提交交易的背书节点，以及共识节点
        Map<String, List<Peer>> orgmspPeerMap = CommonChaincode.buildPeer(hfClient);
        List<Peer> peers = orgmspPeerMap.get(Config.ORG1_MSP);
        for (Peer peer : peers) {
            channel.addPeer(peer);
        }

        channel.addOrderer(CommonChaincode.buildOrderer(hfClient));

        //step5. 初始化链码
        channel.initialize();

        //step6. 构建链码id
        ChaincodeID cid = ChaincodeID.newBuilder().setName("mycc").setVersion("1.0").build();

        //step7. 构建请求参数
        QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
        queryByChaincodeRequest.setChaincodeID(cid);
        queryByChaincodeRequest.setFcn("query");
        queryByChaincodeRequest.setArgs(Lists.newArrayList("a"));

        //step8. 发送查询请求到区块链服务
        Collection<ProposalResponse> proposalResponses = channel.queryByChaincode(queryByChaincodeRequest);
        for (ProposalResponse resp : proposalResponses) {
            System.out.format("message:%s\n", resp.getMessage());
            System.out.format("rsp message a balance is => %s\n", resp.getProposalResponse().getResponse().getPayload().toStringUtf8());
        }

    }
}
