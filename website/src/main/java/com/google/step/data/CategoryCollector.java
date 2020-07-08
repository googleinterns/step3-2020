package com.google.step.data;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.function.*;

public class CategoryCollector implements Collector<String, KeyFactory.Builder, Key> {
    private Key root; 
    
    public CategoryCollector() {
      this.root = KeyFactory.createKey("root", "root"); 
    }

    public static CategoryCollector toKey() {
      return new CategoryCollector();
    }

    @Override
    public Supplier<KeyFactory.Builder> supplier() {
      return () -> new KeyFactory.Builder(root);
    }

    @Override
    public BiConsumer<KeyFactory.Builder, String> accumulator() {
      return (curKey, category) -> curKey.addChild("class", category);
    }

    @Override
    public Function<KeyFactory.Builder, Key> finisher() {
      return KeyFactory.Builder::getKey;
    }

    @Override
    public BinaryOperator<KeyFactory.Builder> combiner() {
      return (keyBuilder1, keyBuilder2) -> {
          Key key2 = keyBuilder2.getKey();
          keyBuilder1.addChild(key2.getKind(), key2.getName());
          return keyBuilder1;
      };
    }
    
    @Override
    public Set<Characteristics> characteristics() {
      Set<Characteristics> characteristics = new HashSet<Characteristics>();
      characteristics.add(Collector.Characteristics.valueOf("UNORDERED"));
      return characteristics;
    }
  }
  