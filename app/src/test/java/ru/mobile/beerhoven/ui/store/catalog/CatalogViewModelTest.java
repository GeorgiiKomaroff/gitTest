package ru.mobile.beerhoven.ui.store.catalog;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import ru.mobile.beerhoven.common.FakeContent;
import ru.mobile.beerhoven.domain.repository.IProductRepository;
import ru.mobile.beerhoven.domain.model.Product;
import ru.mobile.beerhoven.presentation.ui.user.store.catalog.ProductListViewModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CatalogViewModelTest {
   private ProductListViewModel mViewModel;
   private MutableLiveData mMutableList;

   @Mock
   private IProductRepository mMockRepo;

   @Mock
   private List<String> mMockList;

   @Rule
   public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

   @Before
   public void setUp() {
      mMutableList = new MutableLiveData<>();
      mMockRepo = mock(IProductRepository.class);
      mViewModel = new ProductListViewModel(mMockRepo);
      Mockito.when(mMockRepo.getProductList()).thenReturn(mMutableList);
      setFakeItems();
   }

   private void setFakeItems() {
      List<Product> productList = new ArrayList<>();
      productList.add(FakeContent.fakePost);
      mMutableList.setValue(productList);
   }

   @Test
   public void test_returns_not_assertion_error() {
      String expected = "Not assertion error!";
      when(mMockList.get(0)).thenReturn(expected);
      String actual = mMockList.get(0);
      assertEquals(expected, actual);
      System.out.println(actual);
   }

   @Test
   public void viewModel_getCatalogList_returns_what_not_null() {
      // Act
      LiveData<List<Product>> result = mViewModel.getCatalogList();
      // Assert
      assertNotNull(result);
   }

   @Test
   public void viewModel_getCatalogList_returns_what_repository_readList() {
      // Act
      LiveData<List<Product>> result = mViewModel.getCatalogList();
      // Assert
      assertEquals(mMutableList, result);
   }

   @Test
   public void viewModel_getCatalogList_should_calls_method_readProductList() {
      // Act
      mViewModel.getCatalogList();
      // Assert
      verify(mMockRepo, times(1)).getProductList();
   }
}
