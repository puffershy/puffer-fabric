/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.puffer.fabric.client;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class for HFClient.
 *
 * @author Balaji Kadambi
 */

public class FabricClient {

    private HFClient instance;

    /**
     * Return an instance of HFClient.
     *
     * @return
     */
    public HFClient getInstance() {
        return instance;
    }

    public FabricClient() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        instance = HFClient.createNewInstance();
        instance.setCryptoSuite(cryptoSuite);
    }

    /**
     * Constructor
     *
     * @param context
     * @throws CryptoException
     * @throws InvalidArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public FabricClient(User context)
            throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        instance = HFClient.createNewInstance();
        instance.setCryptoSuite(cryptoSuite);
        instance.setUserContext(context);
    }

    /**
     * Create a channel client.
     *
     * @param name
     * @return
     * @throws InvalidArgumentException
     */
    public ChannelClient createChannelClient(String name) throws InvalidArgumentException {
        Channel channel = instance.newChannel(name);
        ChannelClient client = new ChannelClient(name, channel, this);
        return client;
    }

    /**
     * Deploy chain code.
     *
     * @param chainCodeName      链码别名
     * @param remoteLocate       远程目录，用于部署合约
     * @param chaincodeSourceLoc 本地源码路径
     * @param chaincodePath 合约路径，java语言不用设置
     * @param language 合约语言类型
     * @param version 合约版本
     * @param peers 背书节点
     * @return
     * @throws InvalidArgumentException
     * @throws IOException
     * @throws ProposalException
     */
    public Collection<ProposalResponse> deployChainCode(String chainCodeName, String remoteLocate, String chaincodeSourceLoc, String chaincodePath,
            TransactionRequest.Type language, String version, Collection<Peer> peers)
    //  {chaincodeSourceLoc}/src/{chaincodePath}/
            throws InvalidArgumentException, IOException, ProposalException {
        InstallProposalRequest request = instance.newInstallProposalRequest();
        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chainCodeName).setVersion(version)
                .setPath(remoteLocate);
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();
        Logger.getLogger(FabricClient.class.getName()).log(Level.INFO,
                "Deploying chaincode " + chainCodeName + " using Fabric client " + instance.getUserContext().getMspId()
                        + " " + instance.getUserContext().getName());
        request.setChaincodeID(chaincodeID);
        request.setUserContext(instance.getUserContext());
        //		request.setChaincodeInputStream(new FileInputStream(codepath));  // 必须gzip
        request.setChaincodeSourceLocation(new File(chaincodeSourceLoc));
        request.setChaincodePath(chaincodePath);
        request.setChaincodeLanguage(language);
        request.setChaincodeVersion(version);
        Collection<ProposalResponse> responses = instance.sendInstallProposal(request, peers);
        return responses;
    }

}
