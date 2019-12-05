package com.puffer.fabric;

import com.puffer.fabric.client.ChannelClient;
import com.puffer.fabric.client.FabricClient;
import com.puffer.fabric.config.Config;
import com.puffer.fabric.user.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 发布链码方法
 *
 * @author buyi
 * @date 2019年12月03日 19:36:21
 * @since 1.0.0
 */
@Slf4j
public class Deploy {
    //发布路径
    private static final String rootPath = Deploy.class.getResource("/").getPath();

    public static void main(String[] args) throws Exception {
        //step1. 构建组织用户上下文
        List<UserContext> userContextList = CommonChaincode.buildUserContext();

        //step2. 创建客户端
        FabricClient fabricClient = buildFabricClient();

        //step2. 构建组织节点
        Map<String, List<Peer>> orgmspPeerMap = CommonChaincode.buildPeer(fabricClient.getInstance());

        //step3.发布链码到相应组织节点
        depolyChaincode(userContextList, fabricClient, orgmspPeerMap);

        //step4.初始化链码
        intChaincode(fabricClient, orgmspPeerMap);
    }

    /**
     * 发布链码
     *
     * @param fabricClient
     * @param orgmspPeerMap
     * @return
     * @author buyi
     * @date 2019年12月03日 20:56:11
     * @since 1.0.8
     */
    private static void intChaincode(FabricClient fabricClient, Map<String, List<Peer>> orgmspPeerMap)
            throws InvalidArgumentException, TransactionException, ChaincodeEndorsementPolicyParseException, ProposalException, IOException {
        //step1. 构建通道：“mychannel”
        Channel mychannel = fabricClient.getInstance().newChannel(Config.CHANNEL_NAME);

        //step2. 设置通道的共识节点
        Orderer orderer = CommonChaincode.buildOrderer(fabricClient.getInstance());
        mychannel.addOrderer(orderer);

        //step3.设置初始化节点
        for (List<Peer> peers : orgmspPeerMap.values()) {
            for (Peer peer : peers) {
                mychannel.addPeer(peer);
            }
        }

        //step4. 初始化通道
        mychannel.initialize();

        //step5. 构建通道包装类
        ChannelClient channelClient = new ChannelClient(mychannel.getName(), mychannel, fabricClient);

        //step6. 发起初始化链码请求
        String[] arguments = { "a", "1500", "b", "2500" };
        Collection<ProposalResponse> proposalResponses = channelClient.instantiateChainCode(
                Config.CHAINCODE_1_NAME,
                Config.CHAINCODE_1_VERSION,
                Config.CHAINCODE_1_PATH,
                TransactionRequest.Type.JAVA.toString(),
                "init",
                arguments,
                null);

        for (ProposalResponse proposalResponse : proposalResponses) {
            log.info(Config.CHAINCODE_2_NAME + "- Chain code instantiation , status:" + proposalResponse.getStatus() + ",msg:" + proposalResponse.getMessage());
        }
    }

    /**
     * 初始化链码
     *
     * @param userContextList
     * @param fabricClient
     * @param orgmspPeerMap
     * @return
     * @author buyi
     * @date 2019年12月03日 20:55:59
     * @since 1.0.8
     */
    private static void depolyChaincode(List<UserContext> userContextList, FabricClient fabricClient, Map<String, List<Peer>> orgmspPeerMap)
            throws InvalidArgumentException, IOException, ProposalException {
        for (UserContext userContext : userContextList) {
            //设置客户端操作用户
            fabricClient.getInstance().setUserContext(userContext);

            //获取组织机构的节点，作为背书节点
            List<Peer> peers = orgmspPeerMap.get(userContext.getMspId());

            //发布链码到指点节点
            Collection<ProposalResponse> proposalResponses = fabricClient.deployChainCode(Config.CHAINCODE_1_NAME,
                    Config.CHAINCODE_1_PATH,
                    Config.CHAINCODE_ROOT_DIR,
                    null,
                    TransactionRequest.Type.JAVA,
                    Config.CHAINCODE_1_VERSION,
                    peers);

            for (ProposalResponse proposalResponse : proposalResponses) {
                log.info(Config.CHAINCODE_2_NAME + "- Chain code deployment " + userContext.getMspId() + ", status:" + proposalResponse.getStatus() + ",msg:" + proposalResponse.getMessage());
            }

        }
    }

    /**
     * 构建客户端
     *
     * @param
     * @return com.puffer.fabric.client.FabricClient
     * @author buyi
     * @date 2019年12月03日 19:46:10
     * @since 1.0.8
     */
    private static FabricClient buildFabricClient()
            throws IllegalAccessException, InvalidArgumentException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, CryptoException {
        FabricClient fabricClient = new FabricClient();
        return fabricClient;
    }



}
