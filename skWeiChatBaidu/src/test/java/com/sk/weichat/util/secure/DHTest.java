package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DHTest {
    private static final DH.DHKeyPair keyPairIos = new DH.DHKeyPair(
            Base64.decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEMcEC9/rc2AGOiRMeDVB9qHE3R710ianRWhae4LlmFke8FJL4+egmPoRuOri088pM6C0T7bDC8yNw4aMXm9rlzg=="),
            Base64.decode("MHQCAQEEICSLtVso0j2ed3gEuYGB6KwI4iGXEqeZ6wVEJBdMd5JooAcGBSuBBAAKoUQDQgAEMcEC9/rc2AGOiRMeDVB9qHE3R710ianRWhae4LlmFke8FJL4+egmPoRuOri088pM6C0T7bDC8yNw4aMXm9rlzg==")
    );
    private static final DH.DHKeyPair keyPairJs = new DH.DHKeyPair(
            HEX.decode("3056301006072a8648ce3d020106052b8104000a03420004e3f46e09bba6ade8262f1940da6050cf3a1a804d631536803ff9521ff84be06fece6cec6432a4b31e68b335114f682817a2807300e905135aed808457bf682ae"),
            HEX.decode("307402010104209a4b0a6d7bee51f688d8caf9e98d90fa0a40ea3516b9092d55d0f2bed13565d4A00706052B8104000AA14403420004e3f46e09bba6ade8262f1940da6050cf3a1a804d631536803ff9521ff84be06fece6cec6432a4b31e68b335114f682817a2807300e905135aed808457bf682ae")
    );
    private static final DH.DHKeyPair keyPairBc = new DH.DHKeyPair(
            Base64.decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAECnIoYlaZ7IK2u7rSKpG86kF9JRTy05/8qtn3YLRmnhPj6P/Y+vA/M3jC7snfeuBhvdqda+8QL6XIP6SP0wchWA=="),
            Base64.decode("MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgztDVaP0kV3WSQ7MX58KDwGxYlFkcRKt4cwWVvlP7KrGgBwYFK4EEAAqhRANCAAQKcihiVpnsgra7utIqkbzqQX0lFPLTn/yq2fdgtGaeE+Po/9j68D8zeMLuyd964GG92p1r7xAvpcg/pI/TByFY")
    );
    private static final DH.DHKeyPair keyPairCs = new DH.DHKeyPair(
            Base64.decode(
                    "MIIBMzCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////////////////////////////////////v///C8wRAQgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHBEEEeb5mfvncu6xVoGKVzocLBwKb/NstzijZWfKBWxb4F5hIOtp3JqPEZV2k+/wOEQio/Re0SKaFVBmcR9CP+xDUuAIhAP////////////////////66rtzmr0igO7/SXozQNkFBAgEBA0IABGL0n9mPuUC3TVpSHc6uufIvt2b+XsZHsg1w2qRtwDUY6DUKdUvr7pcfMDCfQNr4+Ae7DoCPBgj3lgzaycHUpKM="),
            Base64.decode(
                    "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////////////////////////////////////v///C8wRAQgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHBEEEeb5mfvncu6xVoGKVzocLBwKb/NstzijZWfKBWxb4F5hIOtp3JqPEZV2k+/wOEQio/Re0SKaFVBmcR9CP+xDUuAIhAP////////////////////66rtzmr0igO7/SXozQNkFBAgEBBIIBVTCCAVECAQEEID5o31eWEuIg/VHvJX2QzTnv0s8itKPz6QDq4hrW/JgUoIHjMIHgAgEBMCwGByqGSM49AQECIQD////////////////////////////////////+///8LzBEBCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcEQQR5vmZ++dy7rFWgYpXOhwsHApv82y3OKNlZ8oFbFvgXmEg62ncmo8RlXaT7/A4RCKj9F7RIpoVUGZxH0I/7ENS4AiEA/////////////////////rqu3OavSKA7v9JejNA2QUECAQGhRANCAARi9J/Zj7lAt01aUh3OrrnyL7dm/l7GR7INcNqkbcA1GOg1CnVL6+6XHzAwn0Da+PgHuw6AjwYI95YM2snB1KSj")
    );

    @Test
    public void genKeyPair() throws Exception {
        DH.DHKeyPair keyPair = DH.genKeyPair();
        DH.DHKeyPair keyPair0 = DH.genKeyPair();
        testKeyPair(keyPair, keyPair0);
        testKeyPair(keyPair, keyPairIos);
        testKeyPair(keyPair, keyPairBc);
        testKeyPair(keyPair, keyPairCs);
        testKeyPair(keyPair, keyPairJs);
        testKeyPair(keyPairBc, keyPairCs);
    }

    private void testKeyPair(DH.DHKeyPair kp1, DH.DHKeyPair kp2) throws Exception {
        byte[] sk1 = DH.getCommonSecretKey(kp1.getPrivateKey(), kp2.getPublicKey());
        byte[] sk2 = DH.getCommonSecretKey(kp2.getPrivateKey(), kp1.getPublicKey());
        System.out.println(Base64.encode(sk1));
        System.out.println(Base64.encode(sk2));
        assertArrayEquals(sk1, sk2);
    }
}