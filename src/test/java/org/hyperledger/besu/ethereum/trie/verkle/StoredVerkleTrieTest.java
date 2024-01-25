/*
 * Copyright Hyperledger Besu Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.hyperledger.besu.ethereum.trie.verkle;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.trie.NodeUpdater;
import org.hyperledger.besu.ethereum.trie.verkle.factory.StoredNodeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class StoredVerkleTrieTest {

  @Test
  public void testEmptyTrie() {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
  }

  @Test
  public void testOneValue() {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key).orElse(null)).as("Retrieved value").isEqualTo(value);
  }

  @Test
  public void testTwoValuesAtSameStem() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testTwoValuesAtDifferentIndex() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testTwoValuesWithDivergentStemsAtDepth2() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testDeleteThreeValues() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0200000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key3 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddff");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0300000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
    assertThat(storedTrie.get(key3).orElse(null)).isEqualTo(value3);
  }

  @Test
  public void testDeleteThreeValuesWithFlattening() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0200000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key3 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddff");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0300000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.commit(nodeUpdater);

    StoredVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredVerkleTrie<Bytes32, Bytes32>(nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
    assertThat(storedTrie.get(key3).orElse(null)).isEqualTo(value3);
  }

  @Test
  public void testDeleteManyValuesWithDivergentStemsAtDepth2() throws Exception {
    final Map<Bytes, Bytes> map = new HashMap<>();
    StoredVerkleTrie<Bytes, Bytes> trie =
        new StoredVerkleTrie<>(
            new StoredNodeFactory<>(
                (location, hash) -> Optional.ofNullable(map.get(location)), value -> value));

    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
    Bytes32 key0 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45641");
    Bytes32 value0 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key1 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45601");
    Bytes32 value1 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key2 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45602");
    Bytes32 value2 = Bytes32.fromHexString("0x01");
    Bytes32 key3 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45600");
    Bytes32 value3 = Bytes32.fromHexString("0x00");
    Bytes32 key4 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45603");
    Bytes32 value4 =
        Bytes32.fromHexString("0xf84a97f1f0a956e738abd85c2e0a5026f8874e3ec09c8f012159dfeeaab2b156");
    Bytes32 key5 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45604");
    Bytes32 value5 = Bytes32.fromHexString("0x03");
    Bytes32 key6 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45680");
    Bytes32 value6 =
        Bytes32.fromHexString("0x0000010200000000000000000000000000000000000000000000000000000000");
    trie.put(key0, value0);
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.put(key4, value4);
    trie.put(key5, value5);
    trie.put(key6, value6);

    trie.commit(
        new NodeUpdater() {
          @Override
          public void store(final Bytes location, final Bytes32 hash, final Bytes value) {
            map.put(location, value);
          }
        });
    StoredVerkleTrie<Bytes, Bytes> trie2 =
        new StoredVerkleTrie<>(
            new StoredNodeFactory<>(
                (location, hash) -> Optional.ofNullable(map.get(location)), value -> value));
    assertThat(trie2.getRootHash()).isEqualTo(trie.getRootHash());
    trie2.remove(key0);
    trie2.remove(key4);
    trie2.remove(key5);
    trie2.remove(key6);
    trie2.remove(key3);
    trie2.remove(key1);
    trie2.remove(key2);
    assertThat(trie2.getRootHash()).isEqualTo(Bytes32.ZERO);
  }
}
