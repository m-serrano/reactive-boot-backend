package com.example.demo;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/posts")
public class RxJava2PostController {
    private final RxJava2PostRepository posts;

    public RxJava2PostController(RxJava2PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Observable<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Single<Post> get(@PathVariable(value = "id") Long id) {
        return this.posts.findById(id);
    }

    @PostMapping(value = "/save")
    public Single<Post> create(@RequestBody Post post) {
        return this.posts.save(post);
    }

    @PostMapping(value = "/saveAll")
    public Observable<Post> create(@RequestBody List<Post> posts) {
        return this.posts.saveAll(posts);
    }
}