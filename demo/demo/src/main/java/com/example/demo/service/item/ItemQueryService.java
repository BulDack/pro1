package com.example.demo.service.item;


import com.example.demo.repository.item.ItemQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemQueryService {

    private final ItemQueryRepository itemQueryRepository;


    ///이거 아직 안했네

}
